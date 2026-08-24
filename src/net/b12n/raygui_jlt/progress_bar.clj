(ns net.b12n.raygui-jlt.progress-bar
  "raygui [inputs] example — GuiProgressBar, the one control the user cannot
  move.

  It still takes its value by pointer like every other control, because raygui's
  API is uniform, but nothing in the control writes back: the application
  advances the cell. Here a timer does it, wrapping at 100%, with a second bar
  showing the same value against a different range.

  Press SPACE to pause. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 280 :title "raygui [inputs] example - progress bar")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        progress (rg/cell :float 0.0)
        paused (rg/cell :bool false)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (when (rl/key-pressed? rl/KEY-SPACE)
          (rg/reset-cell! paused (not (rg/value paused))))
        (when-not (rg/value paused)
          (let [next (+ (rg/value progress) (* 0.35 (rl/get-frame-time)))]
            (rg/reset-cell! progress (if (> next 1.0) 0.0 next))))

        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/line! :x 20 :y 16 :w 420 :h 16 :text "0 to 1")
        (rg/progress-bar! :x 60 :y 44 :w 320 :h 24 :left "0" :right "1"
                          :cell progress :min 0.0 :max 1.0)
        (rg/label! :x 20 :y 76 :w 420 :h 24
                   :text (str "progress = " (format "%.2f" (rg/value progress))))

        (rg/line! :x 20 :y 108 :w 420 :h 16 :text "the same cell, range 0 to 2")
        (rg/progress-bar! :x 60 :y 136 :w 320 :h 24 :left "0" :right "2"
                          :cell progress :min 0.0 :max 2.0)
        (rg/label! :x 20 :y 168 :w 420 :h 24
                   :text "one value, two ranges: the bar is half as full")

        (rg/label! :x 20 :y 208 :w 420 :h 24
                   :text (if (rg/value paused) "PAUSED - press SPACE" "press SPACE to pause"))
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! progress)
    (rg/free-cell! paused))
  (rl/close-window))
