(ns r99c.routes.seed
  (:require
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [clojure.java.io :as io]
   [r99c.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]
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
            (form-to [:post "/seed-problems"]
                     (anti-forgery-field)
                     (submit-button "seed problems")))}))

(defn- strip-li
  "strip <li> and </li> from s"
  [s]
  (replace-first (replace-first s #"^<li>" "") #"</li>$" ""))

;; FIXME: must admin only.
(defn insert-problems!
  [request]
  (doseq [s (-> "R99.html"
                io/resource
                slurp
                split-lines)]
    (when (starts-with? s "<li>")
      ;;(println s)
      (db/create-problem! {:problem (strip-li s)})))
  (layout/render request "home.html" {:docs "seed problems"}))

(defn seed-routes []
  [ ""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/seed-problems" {:get  problems-form
                      :post insert-problems!}]])
