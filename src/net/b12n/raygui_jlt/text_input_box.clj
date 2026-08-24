(ns net.b12n.raygui-jlt.text-input-box
  "raygui [inputs] example — GuiTextInputBox, a modal prompt built from a title,
  a message, an entry and a button row.

  Two things worth seeing. The button row is one semicolon-separated string and
  the result arrives as an index in an :int cell, exactly like the toggle group:
  raygui passes lists as strings and answers with indices, which avoids arrays at
  the FFI boundary entirely. And the optional secret cell adds a show/hide toggle
  and masks the entry, which is why it is a cell rather than a flag.

  Press a button to dismiss; the last answer is shown behind. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [inputs] example - text input box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        entry (rg/text-cell 64 "hunter2")
        result (rg/cell :int -1)
        secret (rg/cell :bool false)   ; false = masked; true = "secret view active"
        showing (rg/cell :bool true)
        answer (rg/text-cell 96 "(nothing yet)")]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "behind the dialog")
        (rg/label! :x 20 :y 44 :w 420 :h 24
                   :text (str "last answer: " (rg/value answer)))
        (when (rg/button! :x 20 :y 76 :w 160 :h 32 :text "Ask again")
          (rg/reset-cell! showing true)
          (rg/reset-cell! result -1))

        (when (rg/value showing)
          (rg/text-input-box! :x 70 :y 110 :w 320 :h 150
                              :title "Passphrase"
                              :message "Enter your passphrase:"
                              :cell entry
                              :buttons "Cancel;OK"
                              :result result
                              :secret secret)
          ;; -1 means no button pressed yet. 0 is Cancel, 1 is OK: the index
          ;; into the semicolon-separated :buttons string.
          (when (>= (rg/value result) 0)
            (rg/reset-cell! answer
                            (if (= 1 (rg/value result))
                              (str "OK: " (rg/value entry))
                              "cancelled"))
            (rg/reset-cell! showing false)))

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! entry)
    (rg/free-cell! result)
    (rg/free-cell! secret)
    (rg/free-cell! showing)
    (rg/free-cell! answer))
  (rl/close-window))
