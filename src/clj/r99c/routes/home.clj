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

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn problems-page [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page [request]
  (let [num (get-in request [:path-params :num])
        problem (db/get-problem {:num (Integer/parseInt num)})]
    (layout/render request "answer-form.html" {:problem problem})))

(defn- remove-comments [s]
  (apply str
    (remove #(starts-with? % "//") (split-lines s))))

(defn create-answer! [{:as request {:keys [num answer]} :params}]
  (let [login (name (get-in request [:session :identity]))
        md5-val (-> (replace answer #"\s" "")
                    remove-comments
                    md5)]
    (println "md5" md5-val) 
    (db/create-answer! {:login login :num num :answer answer :md5 md5-val})
    (redirect "/problems")))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]])


