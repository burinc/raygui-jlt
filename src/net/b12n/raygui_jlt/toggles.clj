(ns net.b12n.raygui-jlt.toggles
  "raygui [basics] example — the three toggle controls side by side.

  GuiToggle is a single on/off button over a :bool cell. GuiToggleGroup and
  GuiToggleSlider both take their options as one semicolon-separated string and
  report the selected index through an :int cell, which is raygui's usual way of
  passing a list without an array.

  Click any of them; the labels below show the live cell values. See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private modes "DRAFT;REVIEW;FINAL")

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [basics] example - toggles")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        bold (rg/cell :bool true)
        mode (rg/cell :int 1)
        side (rg/cell :int 0)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "GuiToggle - one button, a :bool cell")
        (rg/toggle! :x 20 :y 44 :w 120 :h 30 :text "Bold" :cell bold)

        (rg/label! :x 20 :y 92 :w 420 :h 24 :text "GuiToggleGroup - \"A;B;C\", an :int cell")
        (rg/toggle-group! :x 20 :y 120 :w 100 :h 30 :text modes :cell mode)

        (rg/label! :x 20 :y 168 :w 420 :h 24 :text "GuiToggleSlider - two ends, an :int cell")
        (rg/toggle-slider! :x 20 :y 196 :w 140 :h 30 :text "OFF;ON" :cell side)

        (rg/label! :x 20 :y 250 :w 420 :h 24
                   :text (str "bold=" (rg/value bold)
                              "   mode=" (nth (str/split modes #";")
                                              (rg/value mode))
                              "   side=" (rg/value side)))
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! bold)
    (rg/free-cell! mode)
    (rg/free-cell! side))
  (rl/close-window))
