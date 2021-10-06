(ns r99c.routes.login
  (:require
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [clojure.java.io :as io]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]] ;; add
   [ring.util.http-response :as response]
   ;;
   [buddy.hashers :as hashers]))

(defn login [request]
  (layout/render request "login.html"))

(defn login-post [{{:keys [login password]} :params}]
  (let [user (db/get-user {:login login})]
    ;;(println "user" user)
    (if (and (seq user)
             (= (:login user) login)
             (hashers/check password (:password user)))
      (-> (redirect "/")
          (assoc-in [:session :identity] (keyword login)))
      (redirect "/login"))))

(defn logout [_]
  (-> (redirect "/")
      (assoc :session {})))

(defn register [request]
  (layout/render request "register.html"))

(defn register-post [{params :params}]
  ;; need verification
  (try
    (db/create-user! (assoc (dissoc params :password)
                            :password (hashers/derive (:password params))))
    (redirect "/login")
    (catch Exception e
      (redirect "/register"))))

(defn login-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/login" {:get  login
              :post login-post}]
   ;; FIXME: post
   ["/logout" {:get logout}]
   ["/register" {:get  register
                 :post register-post}]])

