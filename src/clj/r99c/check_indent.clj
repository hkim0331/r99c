(ns r99c.check-indent
  (:require [clojure.string :as str]))

(defn- remove-comments
  [lines]
  (map #(str/replace % #"//.*" "") lines))

(defn- remove-blank-lines
  [lines]
  (filter (partial re-find #"\S") lines))

(defn- remove-close-open
  [lines]
  (remove (partial re-find #"\}.*\{") lines))

(defn- indent
  [s]
  (count (re-find #"^\s*" s)))

(defn- indents
  [lines]
  (map indent lines))

(defn- diff
  [s]
  (map (fn [[a b]] (- b a)) (partition 2 1 s)))

(defn- curly
  [line]
  (cond
    (re-find #"\{" line) 2
    (re-find #"\}" line) -2
    :else 0))

;; must be a multiple arity function
(defn- curlys
  [lines]
  (map curly lines))

;; forward と backward、two pass で。

;; (def skel (-> (slurp "str_rm.c")
;;               str/split-lines
;;               remove-comments
;;               remove-blank-lines
;;               remove-close-open))

(defn- check-aux
  [diff curls only]
  (let [d (concat (map only diff) [0])
        c (map only curls)]
    (= d c)))

(defn check-indent [s]
  (let [lines (-> s
                  str/split-lines
                  remove-comments
                  remove-blank-lines
                  remove-close-open)
        diffs (diff (indents lines))
        curls (curlys lines)]
    (if (and (check-aux diffs curls #(if (= 2 %) 2 0))
             (check-aux (reverse diffs) (reverse curls) #(if (= -2 %) -2 0)))
      "no error"
      "NG")))

(comment
  (check-indent (slurp "str_rm.c")))