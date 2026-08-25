(ns net.b12n.raygui-jlt.window-box
  "raygui [containers] example — GuiWindowBox, a panel with a title bar and a
  close button.

  Closing it does nothing on raygui's side. There is no window object to
  destroy and no visibility flag to clear: the control reports that the close
  button was clicked, and the application stops calling it. Reopening is just
  calling it again.

  Click the x to close, then Reopen. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 320 :title "raygui [containers] example - window box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        open (rg/cell :bool true)
        closes (rg/cell :int 0)
        agree (rg/cell :bool false)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24
                   :text (str "closed " (rg/value closes) " times"))
        (when-not (rg/value open)
          (when (rg/button! :x 20 :y 48 :w 160 :h 32 :text "Reopen")
            (rg/reset-cell! open true)))

        (when (rg/value open)
          (when (rg/window-box! :x 60 :y 90 :w 340 :h 170 :title "Terms")
            (rg/reset-cell! open false)
            (rg/reset-cell! closes (inc (rg/value closes))))
          (rg/label! :x 76 :y 132 :w 310 :h 24 :text "Nothing here is retained.")
          (rg/check-box! :x 76 :y 166 :w 20 :h 20 :text "I understand" :cell agree)
          (when (rg/button! :x 250 :y 214 :w 130 :h 32 :text "OK")
            (rg/reset-cell! open false)
            (rg/reset-cell! closes (inc (rg/value closes)))))

        (rg/status-bar! :x 0 :y 296 :w 460 :h 24
                        :text "  closing is the caller not drawing it again")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! open)
    (rg/free-cell! closes)
    (rg/free-cell! agree))
  (rl/close-window))
