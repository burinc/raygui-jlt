(ns net.b12n.raygui-jlt.combo-box
  "raygui [collections] example — GuiComboBox, the dropdown's simpler sibling.

  It never opens a list: a click advances to the next option in place. That
  makes it stateless in the UI sense — no edit mode to own, no draw-order rule
  to respect — at the cost of being slow to reach a distant option.

  The two controls here share ONE cell, so clicking either moves both: a plain
  demonstration that the cell is the state and the control is only a view of it.
  See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private levels "TRACE;DEBUG;INFO;WARN;ERROR")

(defn -main
  [& _]
  (rl/window! :width 460 :height 260 :title "raygui [collections] example - combo box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        level (rg/cell :int 2)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "click to advance; no list opens")
        (rg/label! :x 20 :y 52 :w 100 :h 30 :text "Log level")
        (rg/combo-box! :x 130 :y 52 :w 180 :h 30 :text levels :cell level)

        (rg/line! :x 20 :y 100 :w 420 :h 16 :text "the same cell, a second view")
        (rg/combo-box! :x 130 :y 124 :w 180 :h 30 :text levels :cell level)

        (rg/label! :x 20 :y 176 :w 420 :h 24
                   :text (str "level = " (nth (str/split levels #";") (rg/value level))
                              "  (index " (rg/value level) ")"))
        (rg/label! :x 20 :y 202 :w 420 :h 24
                   :text "both boxes read one cell, so both move together")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! level))
  (rl/close-window))
