(ns net.b12n.raygui-jlt.sliders
  "raygui [inputs] example — GuiSlider and GuiSliderBar over :float cells.

  The two differ only in appearance: the slider draws a handle on a plain track,
  the slider bar fills the track up to the value. Both write straight into a
  :float cell, which is the whole of their state.

  The bottom row feeds a slider's value into a raylib circle, showing that a
  control value is just a number once it is read back: nothing about it is
  raygui-specific after (rg/value ...). Drag any of them. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 320 :title "raygui [inputs] example - sliders")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        volume (rg/cell :float 0.35)
        balance (rg/cell :float 0.0)
        radius (rg/cell :float 28.0)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/line! :x 20 :y 16 :w 420 :h 16 :text "GuiSlider - a handle on a track")
        (rg/slider! :x 60 :y 44 :w 320 :h 20 :left "0" :right "1"
                    :cell volume :min 0.0 :max 1.0)
        (rg/label! :x 20 :y 70 :w 420 :h 24
                   :text (str "volume = " (format "%.2f" (rg/value volume))))

        (rg/line! :x 20 :y 102 :w 420 :h 16 :text "GuiSliderBar - a filling track")
        (rg/slider-bar! :x 60 :y 130 :w 320 :h 20 :left "-1" :right "+1"
                        :cell balance :min -1.0 :max 1.0)
        (rg/label! :x 20 :y 156 :w 420 :h 24
                   :text (str "balance = " (format "%.2f" (rg/value balance))))

        (rg/line! :x 20 :y 188 :w 420 :h 16 :text "a value is just a number")
        (rg/slider! :x 60 :y 216 :w 320 :h 20 :left "4" :right "60"
                    :cell radius :min 4.0 :max 60.0)
        (rl/circle! :x 230 :y 274 :radius (rg/value radius) :color rl/SKYBLUE)

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! volume)
    (rg/free-cell! balance)
    (rg/free-cell! radius))
  (rl/close-window))
