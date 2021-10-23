(ns r99c.charts)

(defn- acc-aux [coll ret]
  (if (empty? coll)
    ret
    (acc-aux (rest coll) (conj ret (+ (last ret) (first coll))))))

(defn- acc [coll]
  (acc-aux coll [0]))

;;(defn- line-chart [coll w h])

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
