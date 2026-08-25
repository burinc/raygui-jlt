(ns net.b12n.raygui-jlt.list-view-ex
  "raygui [collections] example — GuiListViewEx, the list view that takes a real
  string array and reports focus.

  This is one of only two raygui functions taking char** rather than a
  semicolon-separated string, so it is where an array actually crosses the FFI
  boundary. jolt's with-c-string-array builds it, and frees every member plus
  the array itself on the way out; the pointers may not outlive that body, which
  is why the control is called inside it rather than the array being handed back.

  The extra cell over the plain list view is :focus, the row the pointer is over,
  which is not the same as the row that is selected. Move the mouse across the
  list. See README.md."
  (:require
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(def ^:private items
  ["deps.edn" "bb.edn" "raygui.clj" "raylib.clj" "check.clj"
   "basic_controls.clj" "sliders.clj" "list_view_ex.clj"])

(defn -main
  [& _]
  (rl/window! :width 460 :height 300 :title "raygui [collections] example - list view ex")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        scroll (rg/cell :int 0)
        active (rg/cell :int 2)
        focus (rg/cell :int -1)]
    (loop [frame 0]
      (when (rl/keep-running? deadline)
        (rl/begin-drawing)
        (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))

        (rg/label! :x 20 :y 16 :w 420 :h 24 :text "a real char** array, not a joined string")
        (rg/list-view-ex! :x 20 :y 48 :w 220 :h 168 :items items
                          :scroll scroll :cell active :focus focus)

        (rg/label! :x 260 :y 48 :w 180 :h 24 :text "three cells")
        (rg/label! :x 260 :y 78 :w 180 :h 24
                   :text (str "active = " (rg/value active)))
        (rg/label! :x 260 :y 104 :w 180 :h 24
                   :text (str "focus  = " (rg/value focus)))
        (rg/label! :x 260 :y 130 :w 180 :h 24
                   :text (str "scroll = " (rg/value scroll)))
        (rg/label! :x 260 :y 164 :w 180 :h 24
                   :text "focus = pointer, active = clicked")

        (rg/status-bar! :x 0 :y 276 :w 460 :h 24
                        :text (str "  selected: " (nth items (max 0 (rg/value active)))))
        (rl/maybe-screenshot! frame 30)
        (rl/end-drawing)
        (recur (inc frame))))
    (rg/free-cell! scroll)
    (rg/free-cell! active)
    (rg/free-cell! focus))
  (rl/close-window))
