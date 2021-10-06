(ns r99c.seed.core
  (:require
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [clojure.java.io :as io]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]] ;; add
   [ring.util.http-response :as response]
   ;;
   [clojure.string :refer [split-lines starts-with? replace-first]]))

(defn- strip-li
  "strip <li> and </li> from s"
  [s]
  (replace-first (replace-first s #"^<li>" "") #"</li>$" ""))

(defn seed-problems!
  "read resources/R99.html, insert the <li>~</li> contents
   into `problems` table"
  []
  (for [s (-> "R99.html"
              io/resource
              slurp
              split-lines)]
    (when (starts-with? s "<li>")
      ;;(println s))))
      (db/create-problem! {:problem (strip-li s)}))))
