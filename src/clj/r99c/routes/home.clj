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

(defn about-page [request]
  (layout/render request "about.html"))

(defn home-routes []
  [ ""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/about" {:get about-page}]])

