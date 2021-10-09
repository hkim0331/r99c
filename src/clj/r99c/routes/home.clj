(ns r99c.routes.home
  (:require
   [clojure.java.io :as io]
   [clojure.string :refer [split-lines starts-with? replace]]
   [digest :refer [md5]]
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [r99c.middleware :as middleware]
   ;;[ring.util.response]
   [ring.util.http-response :as response]
   [ring.util.response :refer [redirect]]))

(defn home-page
  [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (name (get-in request [:session :identity]))
        n (:count (db/problems-count))]
    (layout/render
      request
     "status.html"
     {:user (db/get-user {:login login})
      :range (range 1 (+ 1 n))
      :solved (db/answers {:login login})})))

(defn problems-page
  "display problems."
  [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page
  "take problem number num as path parameter, prep answer to the
   problem."
  [request]
  (let [num (get-in request [:path-params :num])
        problem (db/get-problem {:num (Integer/parseInt num)})]
    (layout/render request "answer-form.html" {:problem problem})))

(defn- remove-comments [s]
  (apply str (remove #(starts-with? % "//") (split-lines s))))

(defn create-answer!
  "insert answer into answers table, compare the md5 value
   with other answers."
  [{:as request {:keys [num answer]} :params}]
  (let [login (name (get-in request [:session :identity]))
        md5-val (-> (replace answer #"\s" "")
                    remove-comments
                    md5)]
    (println "md5-val" md5-val)
    (db/create-answer! {:login login
                        :num (Integer/parseInt num)
                        :answer answer
                        :md5 md5-val})
    (redirect "/problems")))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get status-page}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]])


