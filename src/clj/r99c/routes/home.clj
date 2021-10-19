(ns r99c.routes.home
  (:require
   [buddy.hashers :as hashers]
   [clj-commons-exec :as exec]
   [clj-time.core :as t]
   [clj-time.local :as l]
   [clj-time.periodic :as p]
   [clojure.string :as str]
   [digest]
   [hiccup.core :refer [html]]
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre]))

(timbre/set-level! :debug)

(defn- to-date-str [s]
  (-> (str s)
      (subs 0 10)))

(defn- make-period
  [yyyy mm dd days]
  (let [start-day (l/to-local-date-time (t/date-time yyyy mm dd))]
    (->> (take days (p/periodic-seq start-day (t/days 1)))
         (map to-date-str))))

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

(defn- acc-aux [coll ret]
  (if (empty? coll)
    ret
    (acc-aux (rest coll) (conj ret (+ (last ret) (first coll))))))

(defn- acc [coll]
  (acc-aux coll [0]))

;;(defn- line-chart [coll w h])

(defn- bar-chart [coll w h]
  (let [n (count coll)
        dx (/ w n)]
    (into
     [:svg {:width w :height h :viewbox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]]
     (for [[x y] (map list (range) coll)]
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "red"}]))))

(defn- ->map [rows]
 (apply merge (map (fn [x] {(:create_at x) (:count x)}) rows)))

(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (login request)
        solved (map #(:num %) (db/answers-by {:login login}))
        status (map #(solved? solved %) (map :num (db/problems)))
        individual (db/answers-by-date-login {:login login})
        all-answers (db/answers-by-date)
        all-answers-map (->map all-answers)
        all-answers-coll (for [d period]
                           (get all-answers-map d 0))
        svg (bar-chart (map #(/ % 2) all-answers-coll) 600 150)]
    (layout/render
     request
     "status.html"
     {:login login
      :status status
      :recents     (db/recent-answers {:n 10})
      :comments    (db/sent-comments {:login login})
      :individual  individual
      :all-answers all-answers
      :all-answers-svg (html svg)})))

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

(defn- remove-comments [s]
  (apply str (remove #(str/starts-with? % "//") (str/split-lines s))))

(defn- strip-answer [s]
  (-> s
      (str/replace #"[ \t]" "")
      remove-comments))

;; https://github.com/hozumi/clj-commons-exec
(defn- validate-answer
  "syntax check by `gcc -fsyntaxonly`"
  [answer]
  (timbre/debug "answer:" answer)
  (if (re-matches #"\s*" (strip-answer answer))
    {}
    (let [r (exec/sh ["gcc" "-xc" "-fsyntax-only" "-"] {:in answer})]
      ;;(timbre/debug "validate-answer:" (:exit @r))
      (:err @r))))

(defn create-answer!
  [{{:keys [num answer]} :params :as request}]
  (if-let [errors (validate-answer answer)]
    (layout/render request "error.html"
                   {:status 406
                    :title "プログラムにエラーがあります。"
                    :message "ブラウザのバックで戻って、修正後、再提出してください。"})
    (try
      (db/create-answer! {:login (login request)
                          :num (Integer/parseInt num)
                          :answer answer
                          :md5 (-> answer strip-answer digest/md5)})
      (redirect "/")
      (catch Exception e
        (redirect (str "/answer/" num))))))

(defn comment-form
  "take answer id as path-parameter, show the answer with
   comment form"
  [request]
  (let [id (Integer/parseInt (get-in request [:path-params :id]))
        answer (db/get-answer-by-id {:id id})
        num (:num answer)
        problem (db/get-problem {:num num})
        comments (db/get-comments {:a_id id})
        same-md5 (db/answers-same-md5 {:md5 (:md5 answer)})]
    (if (db/get-answer {:num num :login (login request)})
      (layout/render request "comment-form.html"
                     {:problem problem
                      :answer answer
                      :comments comments
                      :same-md5 same-md5})
      (layout/render request "error.html"
                     {:status 403
                      :title "Access Forbidden"
                      :message "まず自分で解いてから。"}))))

;; FIXME: better way?
(defn create-comment! [request]
  (let [params (:params request)]
    (db/create-comment! {:from_login (login request)
                         :comment (:comment params)
                         :to_login (:to_login params)
                         :p_num (Integer/parseInt (:p_num params))
                         :a_id (Integer/parseInt (:a_id params))})
    (redirect "/")))

(defn ch-pass [{{:keys [old new]} :params :as request}]
  (let [login (login request)
        user (db/get-user {:login login})]
    (if (and (seq user) (hashers/check old (:password user)))
      (do
        (db/update-user! {:login login :password (hashers/derive new)})
        (redirect "/login"))
      (layout/render request "error.html"
                     {:message "did not match old password"}))))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get status-page}]
   ["/ch_pass" {:post ch-pass}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]
   ["/comment/:id" {:get  comment-form
                    :post create-comment!}]])
