(ns examples-registry
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

;; [display-name  joltc-alias  group  description]
;;
;; Single source of truth: bb.edn's :init requires this instead of inlining the
;; vector. Alias equals name for every row here.
;;
;; Descriptions are capped at 49 characters — see `check-descriptions`, which
;; bb.edn's `examples` task runs so the cap is enforced rather than remembered.
(def examples
  [["basic-controls" "basic-controls" "basics" "button, label and a live click counter"]
   ["icon-buttons" "icon-buttons" "basics" "the embedded 1-bit icon pack on buttons"]
   ["labels-lines" "labels-lines" "basics" "labels, separators and a status bar"]
   ["toggles" "toggles" "basics" "toggle, toggle group and toggle slider"]
   ["text-box" "text-box" "inputs" "an editable text box with edit mode"]
   ["text-input-box" "text-input-box" "inputs" "a modal text prompt with secret toggle"]
   ["spinner-value-box" "spinner-value-box" "inputs" "spinner and value box, clamped and typed"]
   ["sliders" "sliders" "inputs" "slider, slider bar and their value cells"]
   ["progress-bar" "progress-bar" "inputs" "a progress bar driven by a timer"]])

(def groups ["basics" "inputs" "collections" "containers" "dialogs" "color" "styling"])

(defn check-descriptions
  "Returns a seq of [name length] for any row whose description exceeds the cap."
  []
  (keep (fn [row]
          (let [d (nth row 3)]
            (when (> (count d) 49) [(nth row 0) (count d)])))
        examples))

(defn by-group
  []
  (into {} (map (fn [g] [g (filterv (fn [r] (= g (nth r 2))) examples)]) groups)))

(defn max-name-width
  []
  (apply max 1 (map (fn [r] (count (nth r 0))) examples)))

(defn pad
  [s n]
  (str s (str/join (repeat (max 0 (- n (count s))) " "))))

(defn doc-mismatches
  "Rows whose bb.edn task :doc disagrees with the registry description.
  Returns [name expected actual] per offender.

  bb.edn's per-example :doc is a hand-copied literal of the description here,
  because babashka reads :doc before :init runs and so cannot take a computed
  value. The copy therefore gets verified rather than trusted. A nil `actual`
  means the registry has a row with NO bb.edn task at all, which is one of the
  five touchpoints failing silently."
  []
  (let [tasks (:tasks (edn/read-string (slurp "bb.edn")))]
    (keep (fn [row]
            (let [nm (nth row 0)
                  expected (str "▶ " (nth row 3))
                  actual (get-in tasks [(symbol nm) :doc])]
              (when-not (= expected actual) [nm expected actual])))
          examples)))
