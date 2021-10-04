(ns r99c.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[r99c started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[r99c has shut down successfully]=-"))
   :middleware identity})
