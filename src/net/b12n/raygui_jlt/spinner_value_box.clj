(ns net.b12n.raygui-jlt.spinner-value-box
  "raygui [inputs] example — the two integer entry controls.

  GuiSpinner has increment and decrement buttons; GuiValueBox is the same field
  without them, for typing a number directly. Both clamp to their own min and
  max once editing ends, but not while typing: the header says clamping happens
  only when user input finishes, so the cell can transiently hold an
  out-of-range value mid-edit.

  Both carry the edit-mode pattern from text-box. Click either to start typing.
  See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 280 :title "raygui [inputs] example - spinner and value box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        qty (rg/cell :int 3)
        port (rg/cell :int 7888)
        qty-edit (rg/cell :bool false)
        port-edit (rg/cell :bool false)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/line! :x 20 :y 16 :w 420 :h 16 :text "GuiSpinner - stepped")
        (rg/label! :x 20 :y 44 :w 120 :h 30 :text "Quantity")
        (when (rg/spinner! :x 150 :y 44 :w 150 :h 30 :cell qty
                           :min 0 :max 10 :edit? (rg/value qty-edit))
          (rg/reset-cell! qty-edit (not (rg/value qty-edit))))
        (rg/label! :x 310 :y 44 :w 130 :h 30 :text "range 0-10")

        (rg/line! :x 20 :y 92 :w 420 :h 16 :text "GuiValueBox - typed")
        (rg/label! :x 20 :y 120 :w 120 :h 30 :text "Port")
        (when (rg/value-box! :x 150 :y 120 :w 150 :h 30 :cell port
                             :min 1024 :max 65535 :edit? (rg/value port-edit))
          (rg/reset-cell! port-edit (not (rg/value port-edit))))
        (rg/label! :x 310 :y 120 :w 130 :h 30 :text "range 1024-65535")

        (rg/line! :x 20 :y 168 :w 420 :h 16 :text "the cells")
        (rg/label! :x 20 :y 192 :w 420 :h 24
                   :text (str "qty=" (rg/value qty) "   port=" (rg/value port)))
        (rg/label! :x 20 :y 218 :w 420 :h 24
                   :text "raygui clamps min/max when editing ends, not while typing")

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! qty)
    (rg/free-cell! port)
    (rg/free-cell! qty-edit)
    (rg/free-cell! port-edit))
  (rl/close-window))
