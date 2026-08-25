(ns net.b12n.raygui-jlt.floating-window
  "raygui [containers] example — draggable windows, a port of raygui's own
  examples/floating_window.

  raygui has no window manager. Dragging is not a feature to enable: the
  application watches for a press inside a title bar, remembers the grab offset
  and moves its own coordinates. The window box does not know it moved.

  Two windows also make the draw-order rule concrete, since there is no z-order
  either: the one drawn last is the one on top, and clicking a window that is
  behind does not raise it.

  Drag either title bar. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private title-h 24)

(defn- in-title-bar?
  [mx my x y w]
  (and (>= mx x) (<= mx (+ x w)) (>= my y) (<= my (+ y title-h))))

(defn -main
  [& _]
  (rl/window! :width 520 :height 360 :title "raygui [containers] example - floating window")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        gain (rg/cell :float 0.5)
        muted (rg/cell :bool false)]
    (loop [frame 0
           ;; [x y] per window, plus which one is being dragged and the grab
           ;; offset. This is application state, not control state: raygui does
           ;; not know these windows moved.
           wins [[40 60] [230 150]]
           drag nil]
      (when (rl/keep-running? deadline)
        (let [mx (rl/get-mouse-x)
              my (rl/get-mouse-y)
              drag (cond
                     (not (rl/mouse-down? rl/MOUSE-LEFT)) nil
                     (some? drag) drag
                     :else (first
                            (keep-indexed
                             (fn [i [wx wy]]
                               (when (in-title-bar? mx my wx wy 240)
                                 [i (- mx wx) (- my wy)]))
                             ;; reversed: the topmost window grabs first
                             (reverse wins))))
              ;; keep-indexed ran over the reversed vector, so map the index back
              drag (when drag
                     (let [[ri ox oy] drag] [(- (dec (count wins)) ri) ox oy]))
              wins (if drag
                     (let [[i ox oy] drag]
                       (assoc wins i [(- mx ox) (- my oy)]))
                     wins)]

          (rl/begin-drawing)
          (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))
          (rg/label! :x 16 :y 8 :w 480 :h 24
                     :text "drag a title bar; the one drawn last stays on top")

          (let [[[ax ay] [bx by]] wins]
            (rg/window-box! :x ax :y ay :w 240 :h 120 :title "Mixer")
            (rg/label! :x (+ ax 16) :y (+ ay 40) :w 200 :h 24 :text "Gain")
            (rg/slider! :x (+ ax 60) :y (+ ay 70) :w 150 :h 20 :left "0" :right "1"
                        :cell gain :min 0.0 :max 1.0)

            (rg/window-box! :x bx :y by :w 240 :h 120 :title "Output")
            (rg/check-box! :x (+ bx 16) :y (+ by 44) :w 20 :h 20
                           :text "Muted" :cell muted)
            (rg/label! :x (+ bx 16) :y (+ by 76) :w 210 :h 24
                       :text (str "gain " (format "%.2f" (rg/value gain)))))

          (rg/status-bar! :x 0 :y 336 :w 520 :h 24
                          :text "  no window manager: the app moves its own coordinates")
          (rl/maybe-screenshot! frame 30)
          (rl/end-drawing)
          (recur (inc frame) wins drag))))
    (rg/free-cell! gain)
    (rg/free-cell! muted))
  (rl/close-window))
