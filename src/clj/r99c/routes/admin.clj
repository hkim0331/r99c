(ns r99c.routes.admin
  (:require
   [clojure.java.io :as io]
   [r99c.db.core :as db]
   [r99c.layout :as layout]
   [r99c.middleware :as middleware]
   [ring.util.http-response :as response]
   ;;[ring.util.response]
   ;;
   [clojure.string :refer [split-lines starts-with? replace-first]]
   [ring.util.response :refer [redirect]]
   [clojure.pprint :refer [pprint]]))

(defn- strip-li
  "strip <li> and </li> from s"
  [s]
  (replace-first (replace-first s #"^<li>" "") #"</li>$" ""))

(defn seed-problems!
  "rebuild problems table from docs/seed-problems.html."
  [request]
  (let [num (atom 0)]
    (db/delete-problems-all!)
    (doseq [s (-> "docs/seed-problems.html" io/resource slurp split-lines)]
      ;;(println s)
      (when (starts-with? s "<li>")
        (db/create-problem! {:problem (strip-li s) :num (swap! num inc)}))))
  (layout/render request "home.html" {:docs "seed problems done."}))

(defn admin-page [request]
  (layout/render request "admin.html"))

(defn problems-page [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn update-problem! [{:keys [params]}]
  (let [q (update (update params :id #(Integer/parseInt %))
                  :num
                  #(Integer/parseInt %))
        ret (db/update-problem! q)]
    (pprint q)
    (if (= 1 ret)
      (redirect "/admin/problems")
      (redirect "/error.html"))))

(defn users-page [request])

(defn comments-page [request])

(defn admin-routes []
  ["/admin"
   {:middleware [middleware/admin
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get  admin-page}]
   ["/problems" {:get problems-page
                 :post update-problem!}]
   ["/users"    {:get users-page}]
   ["/comments" {:get comments-page}]
   ["/seed-problems" {:post seed-problems!}]])
