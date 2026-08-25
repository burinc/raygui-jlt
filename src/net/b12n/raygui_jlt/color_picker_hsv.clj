(ns net.b12n.raygui-jlt.color-picker-hsv
  "raygui [color] example — the HSV picker, and why raygui ships a second one.

  It looks like GuiColorPicker; it differs in what it stores. The RGB picker
  keeps a Color, so hue and saturation are re-derived from RGB every frame, and
  at zero saturation or zero value there is no hue left to derive: drag a colour
  down to black and its hue is gone when you drag back up. The HSV picker keeps
  h, s and v in a :vector3 cell, so hue survives.

  The readout below shows the cell directly, which is where the difference is
  visible. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn- hsv->rgb
  "HSV to a packed raylib Color, so the swatch can be drawn from the cell.
  h is 0-360, s and v are 0-1."
  [h s v]
  (let [c (* v s)
        h' (/ (mod h 360.0) 60.0)
        x (* c (- 1.0 (abs (- (mod h' 2.0) 1.0))))
        m (- v c)
        [r g b] (cond
                  (< h' 1.0) [c x 0.0]
                  (< h' 2.0) [x c 0.0]
                  (< h' 3.0) [0.0 c x]
                  (< h' 4.0) [0.0 x c]
                  (< h' 5.0) [x 0.0 c]
                  :else      [c 0.0 x])]
    (rl/rgba (int (* 255 (+ r m)))
             (int (* 255 (+ g m)))
             (int (* 255 (+ b m)))
             255)))

(defn -main
  [& _]
  (rl/window! :width 500 :height 340 :title "raygui [color] example - color picker HSV")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        hsv (rg/cell :vector3 [200.0 0.6 0.9])
        panel-hsv (rg/cell :vector3 [40.0 0.8 1.0])]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 12 :w 460 :h 24 :text "the cell holds h, s and v, not a Color")
        (rg/color-picker-hsv! :x 20 :y 44 :w 180 :h 180 :cell hsv)

        (let [[h s v] (rg/value hsv)]
          (rg/label! :x 20 :y 236 :w 220 :h 24
                     :text (format "h %.0f  s %.2f  v %.2f" h s v))
          (rl/rect! :x 20 :y 266 :width 180 :height 28 :color (hsv->rgb h s v)))

        (rg/label! :x 260 :y 12 :w 220 :h 24 :text "GuiColorPanelHSV alone")
        (rg/color-panel-hsv! :x 260 :y 44 :w 180 :h 180 :cell panel-hsv)

        (let [[h s v] (rg/value panel-hsv)]
          (rg/label! :x 260 :y 236 :w 220 :h 24
                     :text (format "h %.0f  s %.2f  v %.2f" h s v))
          (rl/rect! :x 260 :y 266 :width 180 :height 28 :color (hsv->rgb h s v)))

        (rg/status-bar! :x 0 :y 316 :w 500 :h 24
                        :text "  HSV survives a trip through black; RGB does not")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! hsv)
    (rg/free-cell! panel-hsv))
  (rl/close-window))
