(ns r99c.routes.home
  (:require
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

(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (login request)
        solved (map #(:num %) (db/answers-by {:login login}))
        status (map #(solved? solved %) (map :num (db/problems)))]
    (layout/render request "status.html" {:login login :status status})))

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

;; https://github.com/hozumi/clj-commons-exec
(defn- validate-answer
  "syntax check `answer`"
  [answer]
  (let [r (exec/sh ["gcc" "-xc" "-fsyntax-only" "-"] {:in answer})]
    (timbre/debug "validate" @r)
    (:err @r)))

(defn create-answer!
  "insert answer into answers table, compare the md5 value
   with other answers."
  [{:as request {:keys [num answer]} :params}]
  (if-let [error (validate-answer answer)]
    (do
     (timbre/debug "error" error)
     (-> (redirect (str "/answer/" num))
         (assoc :flash {:erros "syntax error"})))
    (let [login (name (get-in request [:session :identity]))
          ;; \n matches to \s
          stripped (-> (str/replace answer #"[ \t]" "")
                       remove-comments)
          md5 (digest/md5 stripped)]
      (timbre/debug "create-answer!")
      (db/create-answer! {:login login
                          :num (Integer/parseInt num)
                          :answer answer
                          :md5 md5})
      (redirect "/"))))

(defn comment-form
  "take answer id as path-parameter, show the answer with
   comment form"
  [request]
  (let [id (Integer/parseInt (get-in request [:path-params :id]))
        answer (db/get-answer-by-id {:id id})
        problem (db/get-problem {:num (:num answer)})
        comments (db/get-comments {:a_id id})]
    (layout/render request "comment-form.html"
                           {:problem problem
                            :answer answer
                            :comments comments})))

;; FIXME: better way?
(defn create-comment! [request]
  (let [params (:params request)]
    (db/create-comment! {:from_login (login request)
                         :comment (:comment params)
                         :to_login (:to_login params)
                         :p_num (Integer/parseInt (:p_num params))
                         :a_id (Integer/parseInt (:a_id params))})
    (redirect "/")))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get status-page}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]
   ["/comment/:id" {:get  comment-form
                    :post create-comment!}]])
