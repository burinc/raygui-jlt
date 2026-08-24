(ns net.b12n.raygui-jlt.labels-lines
  "raygui [basics] example — the controls that carry no state: labels with the
  three text alignments, separators with and without a caption, a placeholder
  box and a status bar.

  Also shows the one thing to watch about raygui styling: style properties are
  GLOBAL and persist across frames, so a control that changes TEXT_ALIGNMENT for
  itself has to put it back, or every later control inherits it. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn- aligned-label!
  "Draw a label at one alignment, then restore the default. The restore is the
  point: without it, every control drawn later this frame inherits it."
  [alignment x y w h text]
  (rg/set-text-alignment! rg/LABEL alignment)
  (rg/label! :x x :y y :w w :h h :text text)
  (rg/set-text-alignment! rg/LABEL rg/TEXT-ALIGN-LEFT))

(defn -main
  [& _]
  (rl/window! :width 460 :height 320 :title "raygui [basics] example - labels and lines")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/line! :x 20 :y 16 :w 420 :h 16 :text "alignment")
        (aligned-label! rg/TEXT-ALIGN-LEFT   20 40 420 24 "left aligned")
        (aligned-label! rg/TEXT-ALIGN-CENTER 20 66 420 24 "centered")
        (aligned-label! rg/TEXT-ALIGN-RIGHT  20 92 420 24 "right aligned")

        (rg/line! :x 20 :y 124 :w 420 :h 16 :text "separators")
        (rg/line! :x 20 :y 148 :w 420 :h 12)
        (rg/label! :x 20 :y 164 :w 420 :h 24 :text "the rule above has no caption")

        (rg/line! :x 20 :y 196 :w 420 :h 16 :text "placeholder")
        (rg/dummy-rec! :x 20 :y 220 :w 200 :h 40 :text "GuiDummyRec")

        (rg/status-bar! :x 0 :y 296 :w 460 :h 24 :text "  GuiStatusBar - no state, no result")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame)))))
  (rl/close-window))
