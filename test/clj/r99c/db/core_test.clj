(ns r99c.db.core-test
  (:require
   [r99c.db.core :refer [*db*] :as db]
   [java-time.pre-java8]
   [luminus-migrations.core :as migrations]
   [clojure.test :refer :all]
   [next.jdbc :as jdbc]
   [r99c.config :refer [env]]
   [mount.core :as mount]))

(use-fixtures
  :once
  (fn [f]
    (mount/start
     #'r99c.config/env
     #'r99c.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))

(deftest test-users
  (jdbc/with-transaction [t-conn *db* {:rollback-only true}]
    (is (= 1 (db/create-user!
              t-conn
              {:sid    "Sam"
               :login  "Smith"
               :name   "smith"
               :password "pass"}
              {})))
    (is (= {:sid    "Sam"
            :login  "Smith"
            :name   "smith"
            :password "pass"}
           (-> (db/get-user t-conn {:login "Smith"} {})
               (select-keys [:sid :login :name :password]))))))
