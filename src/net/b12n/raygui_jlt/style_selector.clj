(ns net.b12n.raygui-jlt.style-selector
  "raygui [styling] example — cycle the vendored .rgs themes, after raygui's own
  examples/style_selector.

  A .rgs file carries the whole appearance: every colour, the metrics and an
  embedded font. Loading one replaces raygui's global style, so every control
  drawn afterwards changes at once, including this example's own controls.

  Two things this example is really testing.

  First, the style is loaded from MEMORY rather than by path. GuiLoadStyle
  resolves its argument against the process working directory, so a path that
  works under `bb` from the repo root breaks anywhere else; reading the bytes
  Clojure-side removes the question.

  Second, every colour on screen comes from style-color, which routes through
  raylib's GetColor. raygui stores style colours as 0xRRGGBBAA and raylib packs
  Colors as 0xAABBGGRR, so reading one as the other swaps red and blue and
  renders a wrong theme that still looks like a theme. The swatch row below is
  drawn from the live style, which is where that would show.

  Click a theme, or press LEFT/RIGHT. See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private names (mapv first rg/styles))

(defn -main
  [& _]
  (rl/window! :width 520 :height 360 :title "raygui [styling] example - style selector")
  (rl/set-target-fps 60)
  (rg/apply-style! 2)
  (let [deadline (rl/auto-quit-deadline)
        current (rg/cell :int 2)
        toggle (rg/cell :bool true)
        amount (rg/cell :float 0.45)]
    (loop [frame 0
           applied 2]
      (when (rl/keep-running? deadline)
        (let [want (cond
                     (rl/key-pressed? rl/KEY-RIGHT)
                     (mod (inc (rg/value current)) (count names))
                     (rl/key-pressed? rl/KEY-LEFT)
                     (mod (dec (rg/value current)) (count names))
                     :else (rg/value current))
              _ (rg/reset-cell! current want)
              applied (if (= want applied)
                        applied
                        (do (rg/apply-style! want) want))]

          (rl/begin-drawing)
          (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

          (rg/label! :x 20 :y 12 :w 480 :h 24
                     :text "a .rgs carries the palette, the metrics and a font")

          ;; The toggle group is the selector: its index IS the current style.
          (rg/toggle-group! :x 20 :y 44 :w 68 :h 30
                            :text (str/join ";" names)
                            :cell current)

          (rg/line! :x 20 :y 92 :w 480 :h 16 :text "these change with the theme")
          (rg/button! :x 20 :y 116 :w 140 :h 32 :text "Button")
          (rg/check-box! :x 180 :y 122 :w 20 :h 20 :text "Toggle" :cell toggle)
          (rg/slider! :x 300 :y 122 :w 180 :h 20 :left "0" :right "1"
                      :cell amount :min 0.0 :max 1.0)
          (rg/panel! :x 20 :y 168 :w 220 :h 110 :text "Panel")
          (rg/label! :x 36 :y 210 :w 190 :h 24 :text "and the font changed too")

          ;; Swatches straight from the live style. Every one goes through
          ;; style-color; a red/blue swap would show here first.
          (rg/label! :x 260 :y 168 :w 240 :h 24 :text "live style colours")
          (rl/rect! :x 260 :y 196 :width 56 :height 28
                    :color (rg/style-color rg/DEFAULT rg/BASE-COLOR-NORMAL))
          (rl/rect! :x 322 :y 196 :width 56 :height 28
                    :color (rg/style-color rg/DEFAULT rg/BORDER-COLOR-NORMAL))
          (rl/rect! :x 384 :y 196 :width 56 :height 28
                    :color (rg/style-color rg/DEFAULT rg/TEXT-COLOR-NORMAL))
          (rl/rect! :x 446 :y 196 :width 56 :height 28
                    :color (rg/style-color rg/DEFAULT rg/LINE-COLOR))
          (rg/label! :x 260 :y 232 :w 240 :h 24 :text "base  border  text  line")

          (rg/status-bar! :x 0 :y 336 :w 520 :h 24
                          :text (str "  " (nth names applied)
                                     "   -   LEFT/RIGHT to change"))
          (rl/maybe-screenshot! frame 30)
          (rl/end-drawing)
          (recur (inc frame) applied))))
    (rg/free-cell! current)
    (rg/free-cell! toggle)
    (rg/free-cell! amount))
  (rl/close-window))
