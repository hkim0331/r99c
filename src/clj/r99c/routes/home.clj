(ns r99c.routes.home
  (:require
   [buddy.hashers :as hashers]
   [clj-commons-exec :as exec]
   [clj-time.core :as t]
   [clj-time.local :as l]
   [clj-time.periodic :as p]
   [clojure.string :as str]
   [digest]
   [r99c.charts :refer [class-chart individual-chart comment-chart]]
   [r99c.check-indent :refer [check-indent]]
   [r99c.db.core :as db]
   [r99c.layout :as layout]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]]
   [selmer.filters :refer [add-filter!]]
   [taoensso.timbre :as timbre]))

(timbre/set-level! :info)

(defn- to-date-str [s]
  (-> (str s)
      (subs 0 10)))

(defn- make-period
  [yyyy mm dd days]
  (let [start-day (l/to-local-date-time (t/date-time yyyy mm dd))]
    (->> (take days (p/periodic-seq start-day (t/days 1)))
         (map to-date-str))))

;; 2021-10-11 から 130 日間
(def ^:private period (make-period 2021 10 11 130))

(defn login
  "return user's login as a string"
  [request]
  (name (get-in request [:session :identity])))

;; https://stackoverflow.com/questions/16264813/clojure-idiomatic-way-to-call-contains-on-a-lazy-sequence
(defn- lazy-contains? [col key]
  (some #{key} col))

(defn- solved?
  [col n]
  {:n n :stat (if (lazy-contains? col n) "solved" "yet")})

(defn- wrap-aux
  [n s]
  (if (< (count s) n)
    s
    (str (subs s 0 n) "\n" (wrap-aux n (subs s n)))))

(defn- wrap
  "fold string `s` at column `n`"
  [n s]
  (str/join "\n" (map (partial wrap-aux n) (str/split-lines s))))

(add-filter! :wrap66  (fn [x] (wrap 66 x)))

(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (login request)
        solved (map #(:num %) (db/answers-by {:login login}))
        individual (db/answers-by-date-login {:login login})
        all-answers (db/answers-by-date)]
    (layout/render
     request
     "status.html"
     {:login login
      :status (map #(solved? solved %) (map :num (db/problems)))
      :individual-chart (individual-chart individual period 600 150)
      :class-chart (class-chart all-answers period 600 150)
      :recents (db/recent-answers {:n 20})
      :recent-comments (db/recent-comments {:n 20})})))

(defn problems-page
  "display problems."
  [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page
  "take problem number `num` as path parameter, prep answer to the
   problem. "
  [request]
  (let [num (Integer/parseInt (get-in request [:path-params :num]))
        problem (db/get-problem {:num num})
        answers (db/answers-to {:num num})]
    (if-let [answer (db/get-answer {:num num :login (login request)})]
      ;; can group when already answered
      (let [answers (group-by #(= (:md5 answer) (:md5 %)) answers)]
        (layout/render request
                       "answer-form.html"
                       {:problem problem
                        :same (answers true)
                        :differ (answers false)}))
      (layout/render request
                     "answer-form.html"
                     {:problem problem
                      :same []
                      :differ answers}))))

(defn- remove-comments
  "remove lines starting from //, which is a comment in C"
  [s]
  (apply
   str
   (interpose "\n" (remove #(str/starts-with? % "//") (str/split-lines s)))))

(defn- strip [s]
  (-> s
      (str/replace #"[ \t]" "")
      remove-comments))

(defn- not-empty? [answer]
  (when-not (re-find #"\S" (strip answer))
    (throw (Exception. "answer is empty"))))

(defn- space-rule?
  "R99 space-char rules"
  [s]
  (when-not (every? nil?
                    [(re-find #"include<" s)
                     (re-find #"\)\{" s)
                     (re-find #"if\(" s)
                     (re-find #"for\(" s)
                     (re-find #"while\(" s)
                     (re-find #"}else" s)
                     (re-find #"else\{" s)
                     (re-find #"\n\s*else" s)
                     (re-find #" \+\+" s)
                     (re-find #"\+\+ " s)])
    (throw (Exception. "against R99 space rules"))))

;; https://github.com/hozumi/clj-commons-exec
(defn- can-compile? [answer]
  (let [r (exec/sh ["gcc" "-xc" "-fsyntax-only" "-"] {:in answer})]
    (timbre/debug "gcc" @r)
    (when-let [err (:err @r)]
      (throw (Exception. err)))))

(defn- validate [answer]
  (try
    (not-empty? (strip answer))
    (space-rule? (remove-comments answer))
    ;; 0.14.5
    (check-indent answer)
    (can-compile? answer)
    (catch Exception e (.getMessage e))))

(defn create-answer!
  [{{:keys [num answer]} :params :as request}]
  (if-let [error (validate answer)]
    (do
      (timbre/info "validation failed" (login request) error)
      (layout/render request "error.html"
                     {:status 406
                      :title error
                      :message "ブラウザのバックで戻って、修正後、再提出してください。"}))
    (try
      (let [{:keys [id]} (db/create-answer!
                          {:login (login request)
                           :num (Integer/parseInt num)
                           :answer answer
                           :md5 (-> answer strip digest/md5)})]
        (timbre/info (str "/comment/" id))
        ;;(redirect (str "/comment/" id)))
        (redirect (str "/answer/" num)))
      (catch Exception _
        (layout/render request "error.html"
                       {:status 406
                        :title "database error"
                        :message "can not insert"})))))


(defn comment-form
  "take answer id as path-parameter, show the answer with
   comment form"
  [request]
  (let [id (Integer/parseInt (get-in request [:path-params :id]))
        answer (db/get-answer-by-id {:id id})
        num (:num answer)]
    (if (db/get-answer {:num num :login (login request)})
      (layout/render request "comment-form.html"
                     {:answer answer
                      :problem  (db/get-problem {:num num})
                      :same-md5 (db/answers-same-md5 {:md5 (:md5 answer)})
                      :comments (db/get-comments {:a_id id})})
      (layout/render request "error.html"
                     {:status 403
                      :title "Access Forbidden"
                      :message "まず自分で解いてから。"}))))

(defn create-comment! [request]
  (let [params (:params request)]
    (db/create-comment! {:from_login (login request)
                         :comment (:comment params)
                         :to_login (:to_login params)
                         :p_num (Integer/parseInt (:p_num params))
                         :a_id (Integer/parseInt (:a_id params))})
    (redirect "/")))

(defn comments-sent [request]
  (let [login (get-in request [:path-params :login])
        sent (db/comments-sent {:login login})]
    (layout/render request "comments-sent.html" {:sent sent})))

(defn comments [request]
  (layout/render request "comments.html"
                 {:comments (drop 20 (db/comments))}))

(defn comments-by-num [request]
  (let [num (Integer/parseInt (get-in request [:path-params :num]))]
    (layout/render request "comments.html"
                   {:comments (db/comments-by-num {:num num})})))

;; (defn ch-pass-form [request]
;;   (layout/render request "ch-pass-form.html" {:login (login request)}))

(defn ch-pass [{{:keys [old new]} :params :as request}]
  (let [login (login request)
        user (db/get-user {:login login})]
    (if (and (seq user) (hashers/check old (:password user)))
      (do
        (db/update-user! {:login login :password (hashers/derive new)})
        (redirect "/login"))
      (layout/render request "error.html"
                     {:message "did not match old password"}))))

(def weeks ["2021-10-11" "2021-10-18" "2021-10-25"
            "2021-11-01" "2021-11-08" "2021-11-15" "2021-11-22" "2021-11-29"
            "2021-12-06" "2021-12-13" "2021-12-20" "2021-12-31"
            "2022-01-03" "2022-01-10" "2022-01-17" "2022-01-24" "2022-01-31"
            "2022-02-07"])

(defn before? [s1 s2]
  (< (compare s1 s2) 0))

(defn count-up [m]
  (reduce + (map :count m)))

;; (defn weekly-aux [weeks indiv ret]
;;   (if (empty? weeks)
;;     ret
;;     (let [[this-week rst]
;;           (partition-by #(before? (first weeks) (:create_at %)) indiv)]
;;       (recur (rest weeks) rst (conj ret (count-up this-week))))))

;; (defn weekly [weeks by-date-login]
;;   (weekly-aux weeks by-date-login []))

;; (defn make-weekly [weeks indiv comments]
;;  (apply map list [weeks indiv comments]))

(defn bin-count [data bin]
  (loop [data data bin bin ret []]
    (if (empty? bin)
      ret
      (let [p (group-by #(before? (:create_at %) (first bin)) data)
            f (p true)
            s (p false)]
        (recur s (rest bin)(conj ret (count-up f)))))))

(defn profile [request]
  (let [login (login request)
        solved (db/answers-by {:login login})
        individual (db/answers-by-date-login {:login login})
        comments (db/comments-by-date-login {:login login})]
    (layout/render request "profile.html"
                   {:login login
                    :user (db/get-user {:login login})
                    :chart (individual-chart individual period 600 150)
                    :comment-chart (comment-chart comments period 600 150)
                    :comments-rcvd (db/comments-rcvd {:login login})
                    :comments (db/sent-comments {:login login})
                    :solved (->> solved (map :num) distinct count)
                    :submissions (-> solved count)
                    :last (apply max-key :id solved)
                    :weekly (map list
                             weeks
                             (bin-count individual weeks)
                             (bin-count comments weeks))
                    :groups (filter #(< 200 (:num %)) solved)})))

(defn ranking [request]
  (layout/render request "ranking.html"
                 {:top-n (db/top-users {:n 30})
                  :top-distinct-n (db/top-users-distinct {:n 30})
                  :comments (db/comments-counts {:n 30})}))

(defn home-routes []
  ["" {:middleware [middleware/auth
                    middleware/wrap-csrf
                    middleware/wrap-formats]}
   ["/" {:get status-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]
   ["/ch-pass" {:post ch-pass}]
   ["/comment/:id" {:get  comment-form
                    :post create-comment!}]
   ["/comments" {:get comments}]
   ["/comments-sent/:login" {:get comments-sent}]
   ["/comments/:num" {:get comments-by-num}]
   ["/problems" {:get problems-page}]
   ["/profile" {:get profile}]
   ["/ranking" {:get ranking}]])
