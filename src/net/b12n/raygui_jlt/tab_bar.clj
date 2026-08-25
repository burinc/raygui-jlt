(ns net.b12n.raygui-jlt.tab-bar
  "raygui [collections] example — GuiTabBar, and the one result value that is
  not a yes or no.

  Every other control answers \"nothing happened\" or \"something changed\". The
  tab bar has a third answer: RESULT-TAB-CLOSE, meaning the user clicked a tab's
  close box. raygui does not own the tab list, so it cannot remove anything; it
  reports which tab and the application decides.

  Here closing a tab really removes it, so the list shrinks. Click a tab to
  select, click its x to close. See README.md."
  (:require
   [clojure.string :as str]
   [net.b12n.raygui-jlt.raygui :as rg]
   [net.b12n.raygui-jlt.raylib :as rl]))

(defn -main
  [& _]
  (rl/window! :width 480 :height 280 :title "raygui [collections] example - tab bar")
  (rl/set-target-fps 60)
  (let [deadline (rl/auto-quit-deadline)
        hscroll (rg/cell :int 0)
        active (rg/cell :int 1)]
    (loop [frame 0
           tabs ["README" "deps.edn" "raygui.clj" "NOTICE"]]
      (if (rl/keep-running? deadline)
        (let [result (do
                       (rl/begin-drawing)
                       (rl/clear-background (rg/style-color rg/DEFAULT rg/BACKGROUND-COLOR))
                       (rg/label! :x 20 :y 12 :w 440 :h 24
                                  :text "click a tab to select, its x to close")
                       (rg/tab-bar! :x 20 :y 44 :w 440 :h 28
                                    :text (str/join ";" tabs)
                                    :hscroll hscroll :cell active))
              closing (when (= rg/RESULT-TAB-CLOSE result) (rg/value active))
              tabs' (if (and closing (> (count tabs) 1))
                      (vec (concat (subvec tabs 0 closing)
                                   (subvec tabs (inc closing))))
                      tabs)]
          ;; Clamp the selection: the tab it pointed at may no longer exist.
          (when (>= (rg/value active) (count tabs'))
            (rg/reset-cell! active (dec (count tabs'))))

          (rg/dummy-rec! :x 20 :y 84 :w 440 :h 120
                         :text (str "content of " (nth tabs' (max 0 (min (rg/value active)
                                                                         (dec (count tabs')))))))
          (rg/label! :x 20 :y 216 :w 440 :h 24
                     :text (str (count tabs') " tabs, active = " (rg/value active)))
          (rg/status-bar! :x 0 :y 256 :w 480 :h 24
                          :text "  RESULT-TAB-CLOSE says which tab; you remove it")
          (rl/maybe-screenshot! frame 30)
          (rl/end-drawing)
          (recur (inc frame) tabs'))
        nil))
    (rg/free-cell! hscroll)
    (rg/free-cell! active))
  (rl/close-window))
