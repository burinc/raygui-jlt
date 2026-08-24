(ns net.b12n.raygui-jlt.dropdown-box
  "raygui [collections] example — GuiDropdownBox, and the one layout rule
  immediate mode imposes.

  An open dropdown paints its list OUTSIDE its own bounds, and raygui has no
  z-order: draw order is paint order. So a dropdown has to be drawn last, or
  whatever comes after it paints over the open list. This example draws its
  content first and the two dropdowns last, deliberately.

  Click a dropdown to open it. See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private fonts "Inter;Fira Code;JetBrains Mono;IBM Plex")
(def ^:private sizes "12;14;16;18;20")

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [collections] example - dropdown box")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        font (rg/cell :int 2)
        size (rg/cell :int 1)
        font-open (rg/cell :bool false)
        size-open (rg/cell :bool false)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        ;; Everything that must sit BEHIND an open list is drawn first.
        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "an open list paints outside its bounds")
        (rg/label! :x 20 :y 150 :w 420 :h 24
                   :text (str "font = " (nth (str/split fonts #";") (rg/value font))))
        (rg/label! :x 20 :y 176 :w 420 :h 24
                   :text (str "size = " (nth (str/split sizes #";") (rg/value size))))
        (rg/dummy-rec! :x 20 :y 210 :w 420 :h 44
                       :text "drawn BEFORE the dropdowns, so a list covers it")
        (rg/status-bar! :x 0 :y 276 :w 460 :h 24
                        :text "  raygui has no z-order: draw order is paint order")

        ;; The dropdowns go last. An open list must not be painted over.
        (rg/label! :x 20 :y 56 :w 100 :h 30 :text "Font")
        (rg/label! :x 240 :y 56 :w 60 :h 30 :text "Size")
        (when (rg/dropdown-box! :x 20 :y 88 :w 200 :h 30 :text fonts
                                :cell font :edit? (rg/value font-open))
          (rg/reset-cell! font-open (not (rg/value font-open))))
        (when (rg/dropdown-box! :x 240 :y 88 :w 100 :h 30 :text sizes
                                :cell size :edit? (rg/value size-open))
          (rg/reset-cell! size-open (not (rg/value size-open))))

        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! font)
    (rg/free-cell! size)
    (rg/free-cell! font-open)
    (rg/free-cell! size-open))
  (rl/close-window))
