(ns r99c.routes.home
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [digest :refer [md5]]
   [r99c.layout :as layout]
   [r99c.db.core :as db]
   [r99c.middleware :as middleware]
   ;;[ring.util.response]
   [ring.util.http-response :as response]
   [ring.util.response :refer [redirect]]))

(defn login
  "return user's login as a string"
  [request]
  (name (get-in request [:session :identity])))

(defn home-page
  [request]
  (layout/render request "home.html" {:docs (-> "docs/docs.md" io/resource slurp)}))

;; https://stackoverflow.com/questions/16264813/clojure-idiomatic-way-to-call-contains-on-a-lazy-sequence
(defn lazy-contains? [col key]
  (some #{key} col))

(defn- solved?
  [col n]
  {:n n :stat (if (lazy-contains? col n) "solved" "yet")})

(defn status-page
  "display user's status. how many problems he/she solved?"
  [request]
  (let [login (login request)
        solved (map #(:num %) (db/answers-by {:login login}))
        status (map #(solved? solved %) (map :num (db/problems)))]
    (layout/render request "status.html" {:login login :status status})))

(defn problems-page
  "display problems."
  [request]
  (layout/render request "problems.html" {:problems (db/problems)}))

(defn answer-page
  "take problem number `num` as path parameter, prep answer to the
   problem. "
  [request]
  (let [num (Integer/parseInt (get-in request [:path-params :num]))
        problem (db/get-problem {:num num})]
    (layout/render
     request
     "answer-form.html"
     {:problem problem
      :answers
      (if-not (seq (db/get-answer {:num num :login (login request)}))
        []
        ;; FIXME: group by md5 value is same or not
        (let [ret (db/answers-to {:num num})]
          ret))})))

(defn- remove-comments [s]
  (apply str (remove #(str/starts-with? % "//") (str/split-lines s))))

(defn create-answer!
  "insert answer into answers table, compare the md5 value
   with other answers."
  [{:as request {:keys [num answer]} :params}]
  (let [login (name (get-in request [:session :identity]))
        md5-val (-> (str/replace answer #"\s" "")
                    remove-comments
                    md5)]
    (println "md5-val" md5-val)
    (db/create-answer! {:login login
                        :num (Integer/parseInt num)
                        :answer answer
                        :md5 md5-val})
    (redirect "/problems")))

(defn home-routes []
  [""
   {:middleware [middleware/auth
                 middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get status-page}]
   ["/problems" {:get problems-page}]
   ["/answer/:num" {:get  answer-page
                    :post create-answer!}]])


