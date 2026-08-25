(ns net.b12n.raygui-jlt.list-view
  "raygui [collections] example — GuiListView over a semicolon-separated string.

  Two cells, because raygui writes to both: one holds the scroll position (the
  index of the first visible row) and one the selection, where -1 means nothing
  is selected. Neither is remembered by the control between frames.

  The list holds more rows than fit, so the scrollbar is live. See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private items
  (str/join ";" ["alpha" "bravo" "charlie" "delta" "echo" "foxtrot"
                 "golf" "hotel" "india" "juliet" "kilo" "lima"]))

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [collections] example - list view")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        scroll (rg/cell :int 0)
        active (rg/cell :int 3)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "12 rows, 6 visible: the scrollbar is live")
        (rg/list-view! :x 20 :y 48 :w 200 :h 168 :text items
                       :scroll scroll :cell active)

        (rg/label! :x 240 :y 48 :w 200 :h 24 :text "two cells, both written")
        (rg/label! :x 240 :y 78 :w 200 :h 24
                   :text (str "active = " (rg/value active)))
        (rg/label! :x 240 :y 104 :w 200 :h 24
                   :text (str "scroll = " (rg/value scroll)))
        (rg/label! :x 240 :y 138 :w 200 :h 24
                   :text (if (neg? (rg/value active))
                           "nothing selected"
                           (str "-> " (nth (str/split items #";") (rg/value active)))))
        (rg/status-bar! :x 0 :y 276 :w 460 :h 24
                        :text "  -1 means no selection")
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! scroll)
    (rg/free-cell! active))
  (rl/close-window))
