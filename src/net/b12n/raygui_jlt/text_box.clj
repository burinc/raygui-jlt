(ns net.b12n.raygui-jlt.text-box
  "raygui [inputs] example — an editable text box, and the edit-mode pattern
  every text-entry control in raygui uses.

  raygui keeps no state, including no memory of which box is being edited. The
  control returns non-zero when it wants the mode toggled and the application
  flips its own flag. Here that flag is a :bool cell, so the same mechanism that
  carries control values carries UI state.

  The buffer is a :text cell, a fixed char array raygui edits in place. Click the
  box to start editing, click again to stop. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private buffer-size 64)

(defn -main
  [& _]
  (rl/window! :width 460 :height 260 :title "raygui [inputs] example - text box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        name-buf (rg/text-cell buffer-size "edit me")
        editing (rg/cell :bool true)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "Click the box to toggle edit mode")
        (when (rg/text-box! :x 20 :y 48 :w 420 :h 36
                            :cell name-buf :edit? (rg/value editing))
          (rg/reset-cell! editing (not (rg/value editing))))

        (rg/line! :x 20 :y 100 :w 420 :h 16 :text "what the cell holds")
        (rg/label! :x 20 :y 124 :w 420 :h 24
                   :text (str "value: \"" (rg/value name-buf) "\""))
        (rg/label! :x 20 :y 150 :w 420 :h 24
                   :text (str "length: " (count (rg/value name-buf))
                              " of " (dec buffer-size) " chars"))
        (rg/label! :x 20 :y 176 :w 420 :h 24
                   :text (str "editing: " (rg/value editing)))

        (rg/status-bar! :x 0 :y 236 :w 460 :h 24
                        :text "  raygui edits the buffer in place")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! name-buf)
    (rg/free-cell! editing))
  (rl/close-window))
