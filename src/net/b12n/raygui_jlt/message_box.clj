(ns net.b12n.raygui-jlt.message-box
  "raygui [dialogs] example — GuiMessageBox, and what modality does not mean.

  raygui draws a dialog; it does not block anything behind it. There is no modal
  loop and no input capture: if the caller keeps drawing the controls underneath,
  they keep responding. Modality is the caller declining to draw the rest.

  The counter behind the dialog keeps running to make that visible. Press a
  button to dismiss. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [dialogs] example - message box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        showing (rg/cell :bool true)
        result (rg/cell :int -1)
        answered (rg/text-cell 48 "(no answer yet)")
        ticks (rg/cell :int 0)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rg/reset-cell! ticks (inc (rg/value ticks)))
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24
                   :text (str "frames behind the dialog: " (rg/value ticks)))
        (rg/label! :x 20 :y 42 :w 420 :h 24
                   :text (str "last answer: " (rg/value answered)))
        (when-not (rg/value showing)
          (when (rg/button! :x 20 :y 76 :w 160 :h 32 :text "Ask again")
            (rg/reset-cell! result -1)
            (rg/reset-cell! showing true)))

        (when (rg/value showing)
          (rg/message-box! :x 90 :y 110 :w 280 :h 130
                           :title "#191#Discard changes?"
                           :message "This cannot be undone."
                           :buttons "Cancel;Discard"
                           :result result)
          ;; ONE-BASED: Cancel is 1, Discard is 2, and 0 is the window's X.
          (when (>= (rg/value result) 0)
            (rg/reset-cell! answered
                            (if (= 2 (rg/value result)) "discarded" "cancelled"))
            (rg/reset-cell! showing false)))

        (rg/status-bar! :x 0 :y 276 :w 460 :h 24
                        :text "  raygui does not block what is behind a dialog")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! showing)
    (rg/free-cell! result)
    (rg/free-cell! answered)
    (rg/free-cell! ticks))
  (rl/close-window))
