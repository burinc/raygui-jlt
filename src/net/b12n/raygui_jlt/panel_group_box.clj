(ns net.b12n.raygui-jlt.panel-group-box
  "raygui [containers] example — GuiPanel and GuiGroupBox, and what a container
  is not.

  Neither one contains anything. They draw a frame; they do not clip, do not own
  children, and do not offset what is drawn inside them. A control appears
  \"inside\" a panel because its coordinates fall within the panel's rectangle
  and it was drawn afterwards, which is all containment means in immediate mode.

  The proof is the last group box: a control is drawn deliberately overflowing
  it, and nothing stops it. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 480 :height 340 :title "raygui [containers] example - panel and group box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        notify (rg/cell :bool true)
        sound (rg/cell :bool false)
        gain (rg/cell :float 0.6)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        ;; A panel is drawn FIRST so the controls land on top of it. Reverse the
        ;; order and the panel's fill hides them.
        (rg/panel! :x 20 :y 20 :w 220 :h 150 :text "Preferences")
        (rg/check-box! :x 36 :y 66 :w 20 :h 20 :text "Notifications" :cell notify)
        (rg/check-box! :x 36 :y 96 :w 20 :h 20 :text "Sound" :cell sound)
        (rg/slider! :x 76 :y 130 :w 140 :h 20 :left "0" :right "1"
                    :cell gain :min 0.0 :max 1.0)

        (rg/group-box! :x 260 :y 20 :w 200 :h 150 :text "Group box")
        (rg/label! :x 276 :y 50 :w 170 :h 24 :text "an outline, no fill")
        (rg/label! :x 276 :y 76 :w 170 :h 24 :text "and no title bar")
        (rg/button! :x 276 :y 110 :w 160 :h 32 :text "Still just drawn")

        (rg/group-box! :x 20 :y 200 :w 200 :h 80 :text "nothing is clipped")
        (rg/button! :x 140 :y 232 :w 200 :h 32 :text "this overflows, deliberately")

        (rg/status-bar! :x 0 :y 316 :w 480 :h 24
                        :text "  a container draws a frame; it does not contain")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! notify)
    (rg/free-cell! sound)
    (rg/free-cell! gain))
  (rl/close-window))
