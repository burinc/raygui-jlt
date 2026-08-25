(ns net.b12n.raygui-jlt.custom-input-box
  "raygui [dialogs] example — a dialog raygui does not ship, built from the
  controls it does, after raygui's own examples/custom_input_box.

  GuiTextInputBox is one fixed arrangement: title, message, entry, buttons. When
  that is not the arrangement you want, there is nothing to subclass and nothing
  to configure. You draw a panel and put controls on it, which is all
  GuiTextInputBox is doing internally.

  This one takes two fields rather than one, which the built-in cannot do at all.
  See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 480 :height 330 :title "raygui [dialogs] example - custom input box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        host (rg/text-cell 64 "localhost")
        port (rg/cell :int 7888)
        host-edit (rg/cell :bool false)
        port-edit (rg/cell :bool false)
        tls (rg/cell :bool true)
        status (rg/text-cell 80 "not connected")]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 12 :w 440 :h 24
                   :text "two fields: GuiTextInputBox cannot do this")

        ;; The dialog: a panel, then controls positioned on it. Panel first, or
        ;; its fill covers them.
        (rg/panel! :x 60 :y 48 :w 360 :h 210 :text "Connect to nREPL")

        (rg/label! :x 76 :y 92 :w 80 :h 30 :text "Host")
        (when (rg/text-box! :x 160 :y 92 :w 244 :h 30
                            :cell host :edit? (rg/value host-edit))
          (rg/reset-cell! host-edit (not (rg/value host-edit))))

        (rg/label! :x 76 :y 132 :w 80 :h 30 :text "Port")
        (when (rg/value-box! :x 160 :y 132 :w 120 :h 30 :cell port
                             :min 1024 :max 65535 :edit? (rg/value port-edit))
          (rg/reset-cell! port-edit (not (rg/value port-edit))))

        (rg/check-box! :x 160 :y 174 :w 20 :h 20 :text "Use TLS" :cell tls)

        (when (rg/button! :x 160 :y 210 :w 110 :h 32 :text "Cancel")
          (rg/reset-cell! status "cancelled"))
        (when (rg/button! :x 284 :y 210 :w 120 :h 32
                          :text (rg/icon-text rg/ICON-OK-TICK "Connect"))
          (rg/reset-cell! status
                          (str (if (rg/value tls) "tls://" "nrepl://")
                               (rg/value host) ":" (rg/value port))))

        (rg/label! :x 20 :y 272 :w 440 :h 24
                   :text (str "status: " (rg/value status)))
        (rg/status-bar! :x 0 :y 306 :w 480 :h 24
                        :text "  a panel plus controls is all a dialog is")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! host)
    (rg/free-cell! port)
    (rg/free-cell! host-edit)
    (rg/free-cell! port-edit)
    (rg/free-cell! tls)
    (rg/free-cell! status))
  (rl/close-window))
