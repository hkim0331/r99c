;;; FIXME: indent 4

(ns r99c.check-indent
  (:require
   [clojure.string :as str]
   [taoensso.timbre :as timbre]))

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

(defn- curlys
  [lines]
  (map curly lines))

(defn- check-aux
  [diff curls only]
  (let [d (concat (map only diff) [0])
        c (map only curls)]
    ;;(timbre/debug d)
    ;;(timbre/debug c)
    (= d c)))

(defn- skel [s]
  (-> s
      str/split-lines
      remove-comments
      remove-blank-lines
      remove-close-open))

(defn check-indent [s]
  (let [lines (skel s)
        indents (indents lines)
        diffs (diff indents)
        curls (curlys lines)]
    (timbre/debug "check-indent invoked")
    (if (and
         (every? even? indents)
         (check-aux diffs curls #(if (= 2 %) 2 0))
         (check-aux (reverse diffs) (reverse curls) #(if (= -2 %) -2 0)))
      "GOOD"
      "NG")))

(comment
  (check-indent (slurp "sample.c")))
