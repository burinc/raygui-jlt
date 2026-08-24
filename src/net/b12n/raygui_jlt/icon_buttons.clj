(ns net.b12n.raygui-jlt.icon-buttons
  "raygui [basics] example — the embedded 1-bit icon pack.

  raygui ships 256 icons inside the header itself, so there is no image file to
  load and nothing to ship alongside the binary. An icon reaches a control
  through its TEXT: GuiIconText prepends a `#nnn#` marker that any control
  renders as the icon. GuiDrawIcon draws one directly, at a pixel size.

  The last row is drawn at icon scale 2 to show that the scale is global and
  persists until it is set back. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 320 :title "raygui [basics] example - icon buttons")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        saved (rg/cell :int 0)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "icons live in the control's text")
        (when (rg/button! :x 20 :y 44 :w 140 :h 32
                          :text (rg/icon-text rg/ICON-FILE-SAVE "Save"))
          (rg/reset-cell! saved (inc (rg/value saved))))
        (rg/button! :x 172 :y 44 :w 140 :h 32
                    :text (rg/icon-text rg/ICON-FOLDER-OPEN "Open"))
        (rg/button! :x 20 :y 84 :w 140 :h 32
                    :text (rg/icon-text rg/ICON-BIN "Delete"))
        (rg/button! :x 172 :y 84 :w 140 :h 32
                    :text (rg/icon-text rg/ICON-GEAR "Settings"))

        (rg/label! :x 20 :y 128 :w 420 :h 24
                   :text (str "saved " (rg/value saved) " times"))

        (rg/line! :x 20 :y 156 :w 420 :h 16 :text "GuiDrawIcon, drawn directly")
        (rg/draw-icon! :icon rg/ICON-OK-TICK   :x 24  :y 184 :size 2 :color rl/DARKGREEN)
        (rg/draw-icon! :icon rg/ICON-CROSS     :x 64  :y 184 :size 2 :color rl/MAROON)
        (rg/draw-icon! :icon rg/ICON-STAR      :x 104 :y 184 :size 2 :color rl/GOLD)
        (rg/draw-icon! :icon rg/ICON-HEART     :x 144 :y 184 :size 2 :color rl/RED)
        (rg/draw-icon! :icon rg/ICON-WARNING   :x 184 :y 184 :size 2 :color rl/ORANGE)

        ;; Icon scale is global. Set it, draw, set it back — the same discipline
        ;; every style property needs.
        (rg/line! :x 20 :y 228 :w 420 :h 16 :text "icon scale 2, then restored")
        (rg/set-icon-scale! 2)
        (rg/button! :x 20 :y 252 :w 180 :h 44
                    :text (rg/icon-text rg/ICON-PLAYER-PLAY "Play"))
        (rg/set-icon-scale! 1)
        (rg/button! :x 212 :y 258 :w 180 :h 32
                    :text (rg/icon-text rg/ICON-ARROW-RIGHT "Next"))

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! saved))
  (rl/close-window))
