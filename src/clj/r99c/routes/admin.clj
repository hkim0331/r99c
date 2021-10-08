(ns r99c.routes.admin
  (:require
   [clojure.java.io :as io]
   [r99c.db.core :as db]
   [r99c.layout :as layout]
   [r99c.middleware :as middleware]
   [ring.util.http-response :as response]
   [ring.util.response]
   ;;
   [clojure.string :refer [split-lines starts-with? replace-first]]
   [hiccup.core :refer [html]]
   [hiccup.form :refer [form-to submit-button]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn problems-form [request]
  (layout/render
   request
   "home.html"
   {:docs (html
           (form-to [:post "/admin/p"]
                    (anti-forgery-field)
                    (submit-button "seed problems")))}))

(defn- strip-li
  "strip <li> and </li> from s"
  [s]
  (replace-first (replace-first s #"^<li>" "") #"</li>$" ""))

(defn insert-problems!
  [request]
  (doseq [s (-> "R99.html"
                io/resource
                slurp
                split-lines)]
    (when (starts-with? s "<li>")
      ;;(println s)
      (db/create-problem! {:problem (strip-li s)})))
  (layout/render request "home.html" {:docs "seed problems done"}))

(defn admin-page [request]
  (layout/render request "home.html" {:docs "admin-page"}))

(defn admin-routes []
  ["/admin"
   {:middleware [middleware/admin
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get  admin-page}]])

