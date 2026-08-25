(ns net.b12n.raygui-jlt.scroll-panel
  "raygui [containers] example — GuiScrollPanel, a port of raygui's own
  examples/scroll_panel.

  The only container that really clips, and the only control taking TWO by-value
  Rectangles in one call: its own bounds and the size of the content behind it.
  raygui writes back the scroll offset and the visible region, so the caller
  knows how to draw the content shifted.

  Everything drawn as content goes inside a scissor region offset by the scroll,
  which is the caller's job: raygui clips its own chrome, not your drawing.
  Drag the scrollbars. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private content-w 560)
(def ^:private content-h 420)

(defn -main
  [& _]
  (rl/window! :width 480 :height 340 :title "raygui [containers] example - scroll panel")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        scroll (rg/cell :vector2 [0.0 0.0])
        view (rg/cell :rect [0.0 0.0 0.0 0.0])]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 12 :w 440 :h 24
                   :text "content is 560x420 inside a 300x200 panel")
        (rg/scroll-panel! :x 20 :y 44 :w 300 :h 200 :text "Content"
                          :content-w content-w :content-h content-h
                          :scroll scroll :view view)

        ;; Draw the content shifted by the scroll offset, clipped to the view
        ;; raygui just reported. raygui clips its own chrome; the content is ours.
        (let [[sx sy] (rg/value scroll)
              [vx vy vw vh] (rg/value view)]
          (rl/begin-scissor-mode (int vx) (int vy) (int vw) (int vh))
          (doseq [row (range 7)
                  col (range 7)]
            (let [cx (+ vx sx 20 (* col 80))
                  cy (+ vy sy 20 (* row 56))]
              (rl/rect! :x (int cx) :y (int cy) :width 64 :height 40
                        :color (if (even? (+ row col)) rl/SKYBLUE rl/LIGHTGRAY))))
          (rl/end-scissor-mode)

          (rg/label! :x 336 :y 44 :w 130 :h 24 :text "raygui writes:")
          (rg/label! :x 336 :y 70 :w 140 :h 24
                     :text (str "scroll " (int sx) "," (int sy)))
          (rg/label! :x 336 :y 96 :w 140 :h 48
                     :text (str "view " (int vw) "x" (int vh))))

        (rg/status-bar! :x 0 :y 316 :w 480 :h 24
                        :text "  two by-value Rectangles: bounds and content")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! scroll)
    (rg/free-cell! view))
  (rl/close-window))
