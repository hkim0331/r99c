(ns r99c.routes.login
  (:require
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [clojure.java.io :as io]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]] ;; add
   [ring.util.http-response :as response]))

(defn login [request]
  (layout/render request "login.html"))

(defn login-post [{{:keys [login password]} :params}]
  (if (= login "hkimura")
    (-> (redirect "/")
        (assoc-in [:session :identity] (keyword login)))
    (redirect "/login")))

(defn logout [request]
  (-> (redirect "/")
      (assoc :session {})))

(defn login-routes []
  [ ""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/login" {:get login
              :post login-post}]
   ;; FIXME: post
   ["/logout" {:get logout}]])
