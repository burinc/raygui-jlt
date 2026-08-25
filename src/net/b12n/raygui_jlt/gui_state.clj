(ns net.b12n.raygui-jlt.gui-state
  "raygui [styling] example — the global state API, and the discipline it needs.

  GuiSetState, GuiSetAlpha, GuiLock and GuiDisable are all global and all
  persistent: they apply to every control drawn afterwards, for as long as the
  program runs, until something sets them back. There is no scope and no stack.

  That makes forgetting to restore one the characteristic raygui bug, and a
  quiet one: the affected controls still draw, just wrong, and often far from
  the code that changed the setting. Every block below sets its value back
  immediately, which is the only reliable habit.

  The bottom row is genuinely locked: click it and nothing happens.
  See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 520 :height 380 :title "raygui [styling] example - gui state")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        clicks (rg/cell :int 0)
        locked-clicks (rg/cell :int 0)
        checked (rg/cell :bool true)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 12 :w 480 :h 24
                   :text "all of this is global and persists until set back")

        ;; The four visual states, forced.
        (rg/line! :x 20 :y 44 :w 480 :h 16 :text "GuiSetState - forced appearance")
        (doseq [[i [label state]] (map-indexed vector
                                               [["normal" rg/STATE-NORMAL]
                                                ["focused" rg/STATE-FOCUSED]
                                                ["pressed" rg/STATE-PRESSED]
                                                ["disabled" rg/STATE-DISABLED]])]
          (rg/set-state! state)
          (rg/button! :x (+ 20 (* i 122)) :y 72 :w 112 :h 32 :text label)
          (rg/set-state! rg/STATE-NORMAL))

        ;; Alpha, set and restored.
        (rg/line! :x 20 :y 120 :w 480 :h 16 :text "GuiSetAlpha - and back to 1.0")
        (rg/set-alpha! 0.35)
        (rg/button! :x 20 :y 148 :w 160 :h 32 :text "35% alpha")
        (rg/set-alpha! 1.0)
        (rg/button! :x 192 :y 148 :w 160 :h 32 :text "restored")

        ;; A working control, for contrast with the locked one below.
        (rg/line! :x 20 :y 196 :w 480 :h 16 :text "live")
        (when (rg/button! :x 20 :y 224 :w 160 :h 32 :text "Click me")
          (rg/reset-cell! clicks (inc (rg/value clicks))))
        (rg/check-box! :x 200 :y 230 :w 20 :h 20 :text "enabled" :cell checked)
        (rg/label! :x 330 :y 224 :w 180 :h 32
                   :text (str "clicks: " (rg/value clicks)))

        ;; GuiLock: drawn normally, but input is ignored entirely.
        (rg/line! :x 20 :y 272 :w 480 :h 16 :text "GuiLock - looks live, ignores input")
        (rg/gui-lock)
        (when (rg/button! :x 20 :y 300 :w 160 :h 32 :text "Locked")
          (rg/reset-cell! locked-clicks (inc (rg/value locked-clicks))))
        (rg/gui-unlock)
        (rg/label! :x 200 :y 300 :w 300 :h 32
                   :text (str "locked clicks: " (rg/value locked-clicks)
                              "  (stays 0)"))

        (rg/status-bar! :x 0 :y 356 :w 520 :h 24
                        :text "  no scope, no stack: set it back yourself")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! clicks)
    (rg/free-cell! locked-clicks)
    (rg/free-cell! checked))
  (rl/close-window))
