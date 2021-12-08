(ns r99c.charts
  (:require [hiccup.core :refer [html]]))

;; FIXME: w h で渡されてくるのは viewbox のサイズ。
;;        プロットが viewbox に収まるように調整するのは bar-char の仕事。
(defn bar-chart [coll w h]
  (let [n (count coll)
        dx (/ w n)]
    (into
     [:svg {:width w :height h :viewbox (str "0 0 " w " " h)}
      [:rect {:x 0 :y 0 :width w :height h :fill "#eee"}]
      [:line {:x1 0 :y1 (- h 10) :x2 w :y2 (- h 10) :stroke "black"}]]
     (for [[x y] (map list (range) coll)]
       [:rect
        {:x (* dx x) :y (- h 10 y) :width (/ dx 2) :height y
         :fill "red"}]))))

(defn- ->date-count
  [coll]
  (zipmap (map (comp val first)  coll)
          (map (comp val second) coll)))

;; s/coll/ys
;; 2.5 は縮尺。
;; FIXME: DRY!
(defn class-chart
  [answers period width height]
  (let [tmp (->date-count answers)
        ys  (for [d period]
              (get tmp d 0))]
    (html (bar-chart (map #(/ % 2.5) ys) width height))))

(defn individual-chart
  [answers period width height]
  (let [tmp (->date-count answers)
        ys  (for [d period]
              (get tmp d 0))]
    (html (bar-chart (map #(* % 13) ys) width height))))

(defn comment-chart
  [answers period width height]
  (let [tmp (->date-count answers)
        ys  (for [d period]
              (get tmp d 0))]
    (html (bar-chart (map #(* % 5) ys) width height))))