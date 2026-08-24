(ns net.b12n.raygui-jlt.basic-controls
  "raygui [basics] example — a button, a label and a checkbox over a live click
  counter. The smallest complete raygui program: it proves the vendored library
  loaded, the by-value Rectangle reached the right bounds, and a :bool cell
  round-tripped through raygui's pointer API.

  Click the button to raise the counter; tick the checkbox to enable the reset
  button. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 420 :height 260 :title "raygui [basics] example - basic controls")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        armed (rg/cell :bool true)
        clicks (rg/cell :int 0)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))
        (rg/label! :x 20 :y 16 :w 380 :h 24 :text "raygui, called from jolt over the C ABI")
        (when (rg/button! :x 20 :y 52 :w 160 :h 32 :text "Click me")
          (rg/reset-cell! clicks (inc (rg/value clicks))))
        (rg/label! :x 200 :y 58 :w 200 :h 24
                   :text (str "clicks: " (rg/value clicks)))
        (rg/check-box! :x 20 :y 104 :w 20 :h 20 :text "Reset enabled" :cell armed)
        (if (rg/value armed)
          (when (rg/button! :x 20 :y 140 :w 160 :h 32 :text "Reset")
            (rg/reset-cell! clicks 0))
          (do (rg/gui-set-state rg/STATE-DISABLED)
              (rg/button! :x 20 :y 140 :w 160 :h 32 :text "Reset")
              (rg/gui-set-state rg/STATE-NORMAL)))
        (rg/label! :x 20 :y 200 :w 380 :h 24
                   :text "raygui keeps no state: these values live in cells")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! armed)
    (rg/free-cell! clicks))
  (rl/close-window))
