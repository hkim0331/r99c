(ns r99c.routes.home
  (:require
   [buddy.hashers :as hashers]
   [clj-commons-exec :as exec]
   [clojure.string :as str]
   [digest]
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]]
   [taoensso.timbre :as timbre]))

(timbre/set-level! :debug)

(defn login
  "return user's login as a string"
  [request]
  (name (get-in request [:session :identity])))

;; https://stackoverflow.com/questions/16264813/clojure-idiomatic-way-to-call-contains-on-a-lazy-sequence
(defn lazy-contains? [col key]
  (some #{key} col))

(defn- solved?
  [col n]
  {:n n :stat (if (lazy-contains? col n) "solved" "yet")})

;; SVG plot
(defn- plot [w h answers]
  (let [n (count answers)
        dx (/ w n)
        counts (map :count answers)]
    (timbre/debug "plot/answers" (first answers))
    (timbre/debug "plot/counts:" (first counts))
    (into
     [:svg {:width w :height h :viewbox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]]
     (for [[x y] (map list (range) counts)]
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "red"}]))))

;; FIXME: client side rendering
(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (login request)
        solved (map #(:num %) (db/answers-by {:login login}))
        status (map #(solved? solved %) (map :num (db/problems)))
        all-answers (db/answers-by-date)]
    (timbre/debug "plot returns:" (plot 300 150 all-answers))
    (layout/render
     request
     "status.html"
     {:login login
      :status status
      :recents     (db/recent-answers {:n 10})
      :my-answers  (db/answers-by-date-login {:login login})
      :all-answers all-answers
      :comments    (db/sent-comments {:login login})})))

(defn problems-page
  "display problems."
  [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page
  "take problem number `num` as path parameter, prep answer to the
   problem. "
  [request]
  (let [num (Integer/parseInt (get-in request [:path-params :num]))
        problem (db/get-problem {:num num})]
    (if-let [answer (db/get-answer {:num num :login (login request)})]
      (let [answers (group-by #(= (:md5 answer) (:md5 %))
                              (db/answers-to {:num num}))]
        (layout/render request
                       "answer-form.html"
                       {:problem problem
                        :same (answers true)
                        :differ (answers false)}))
      (layout/render request
                     "answer-form.html"
                     {:problem problem
                      :same []
                      :differ []}))))

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
                   {:status "can not compile"
                    :title "プログラムにエラーがあります。"
                    :message "ブラウザのバックで修正後、再提出してください。"})
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
        comments (db/get-comments {:a_id id})]
    (if (db/get-answer {:num num :login (login request)})
      (layout/render request "comment-form.html"
                     {:problem problem
                      :answer answer
                      :comments comments})
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
