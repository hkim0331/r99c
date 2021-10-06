(ns r99c.handler
  (:require
    [mount.core :as mount]
    [r99c.env :refer [defaults]]
    [r99c.layout :refer [error-page]]
    [r99c.middleware :as middleware]
    [r99c.routes.home :refer [home-routes]]
    [reitit.ring :as ring]
    [ring.middleware.content-type :refer [wrap-content-type]]
    [ring.middleware.webjars :refer [wrap-webjars]]
    ;;
    [r99c.routes.admin :refer [admin-routes]]
    [r99c.routes.login :refer [login-routes]]))

(mount/defstate init-app
  :start ((or (:init defaults) (fn [])))
  :stop  ((or (:stop defaults) (fn []))))

(mount/defstate app-routes
  :start
  (ring/ring-handler
   (ring/router
    ;;
    [(login-routes) (home-routes) (admin-routes)]
    (ring/routes
     (ring/create-resource-handler
      {:path "/"})
     (wrap-content-type
      (wrap-webjars (constantly nil)))
     (ring/create-default-handler
      {:not-found
       (constantly (error-page {:status 404, :title "404 - Page not found"}))
       :method-not-allowed
       (constantly (error-page {:status 405, :title "405 - Not allowed"}))
       :not-acceptable
       (constantly (error-page {:status 406, :title "406 - Not acceptable"}))})))))

(defn app []
  (middleware/wrap-base #'app-routes))
