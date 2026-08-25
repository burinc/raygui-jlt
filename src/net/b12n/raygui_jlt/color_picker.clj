(ns net.b12n.raygui-jlt.color-picker
  "raygui [color] example — GuiColorPicker, plus the separate panel and bars it
  is assembled from.

  Worth knowing which encoding you have. A :color cell holds a raylib Color,
  already packed 0xAABBGGRR, so a picked colour goes straight into a raylib
  drawing call with no conversion. A colour read from GuiGetStyle is raygui's
  0xRRGGBBAA and must go through style-color first. Two encodings in one library,
  and only the style side needs converting.

  The swatch below is drawn with the picked value, unconverted. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 520 :height 340 :title "raygui [color] example - color picker")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        picked (rg/cell :color (rl/rgba 102 191 255 255))
        alpha (rg/cell :float 1.0)
        hue (rg/cell :float 200.0)
        panel-color (rg/cell :color (rl/rgba 230 41 55 255))]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 12 :w 480 :h 24 :text "GuiColorPicker: square plus hue bar")
        (rg/color-picker! :x 20 :y 44 :w 180 :h 180 :cell picked)
        (rg/color-bar-alpha! :x 20 :y 236 :w 180 :h 20 :cell alpha)

        ;; A :color cell is already a raylib Color: no conversion on this path.
        (rg/label! :x 20 :y 268 :w 180 :h 24 :text "picked, drawn directly")
        (rl/rect! :x 20 :y 292 :width 180 :height 28 :color (rg/value picked))

        (rg/label! :x 240 :y 12 :w 260 :h 24 :text "the pieces, separately")
        (rg/color-panel! :x 240 :y 44 :w 150 :h 180 :cell panel-color)
        (rg/color-bar-hue! :x 400 :y 44 :w 24 :h 180 :cell hue)

        (rg/label! :x 240 :y 236 :w 260 :h 24
                   :text (str "alpha " (format "%.2f" (rg/value alpha))
                              "   hue " (format "%.0f" (rg/value hue))))
        (rg/label! :x 240 :y 268 :w 260 :h 24 :text "panel colour")
        (rl/rect! :x 240 :y 292 :width 184 :height 28 :color (rg/value panel-color))

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! picked)
    (rg/free-cell! alpha)
    (rg/free-cell! hue)
    (rg/free-cell! panel-color))
  (rl/close-window))
