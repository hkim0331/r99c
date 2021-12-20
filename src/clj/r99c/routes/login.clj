(ns r99c.routes.login
  (:require
   [buddy.hashers :as hashers]
   ;;[clojure.java.io :as io]
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [r99c.middleware :as middleware]
   [ring.util.response :refer [redirect]]
   [struct.core :as st]
   [taoensso.timbre :as timbre]))

(def ^:private version "0.14.6")

(def users-schema
  [[:sid
    st/required
    st/string
    {:message "学生番号は数字3つに英大文字、続いて数字4つです。"
     :validate (fn [sid] (re-matches #"^\d{3}[A-Z]\d{4}" sid))}]
   [:name
    st/required
    st/string]
   [:login
    st/required
    st/string
    {:message "同じユーザ名があります。"
     :validate (fn [login]
                  (let [ret (db/get-user {:login login})]
                   (timbre/debug "validate ret:" ret)
                   (empty? ret)))}]
   [:password
    st/required
    st/string]])

(defn validate-user [params]
  (let [ret (st/validate params users-schema)]
    (timbre/debug "validate:" ret)
    (first ret)))

(defn about-page [request]
  (layout/render request "about.html" {:version version}))

(defn admin-only [request]
  (layout/render request "error.html" {:status 401
                                       :title "Unauthorized"
                                       :message "This page is admin only."}))

(defn login [request]
  (layout/render request "login.html"))

(defn login-post [{{:keys [login password]} :params}]
  (let [user (db/get-user {:login login})]
    (if (and (seq user)
             (= (:login user) login)
             (hashers/check password (:password user)))
      (do
       (timbre/info "login success" login)
       (db/login {:login login})
       (-> (redirect "/")
           (assoc-in [:session :identity] (keyword login))))
      (do
       (timbre/info "login faild" login)
       (redirect "/login")))))

(defn logout [_]
  (-> (redirect "/")
      (assoc :session {})))

(defn register [{:keys [flash] :as request}]
  (layout/render request
                 "register.html"
                 {:errors (select-keys flash [:errors])}))

(defn register-post [{params :params}]
  (if-let [errors (validate-user params)]
    (-> (redirect "/register")
        (assoc :flash (assoc params :errors errors)))
    (try
      (db/create-user! (assoc (dissoc params :password)
                              :password (hashers/derive (:password params))))
      (redirect "/login")
      (catch Exception e
        (redirect "/register")))))

(defn login-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/about" {:get about-page}]
   ["/admin-only" {:get admin-only}]
   ["/login" {:get  login
              :post login-post}]
   ;; FIXME: post
   ["/logout" {:get logout}]
   ["/register" {:get  register
                 :post register-post}]])
