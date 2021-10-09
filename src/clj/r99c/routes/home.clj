(ns r99c.routes.home
  (:require
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [clojure.java.io :as io]
   [r99c.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

(defn problems-page [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page [request]
  (let [num (get-in request [:path-params :num])
        problem (db/get-problem {:num (Integer/parseInt num)})]
    (layout/render request "answer-form.html" {:problem problem})))

(defn create-answer! [request]
  (layout/render request "request.html" {:request request}))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]])


