(ns examples-registry
  (:require [clojure.string :as str]))

;; [display-name  joltc-alias  group  description]
;;
;; Single source of truth: bb.edn's :init requires this instead of inlining the
;; vector. Alias equals name for every row here.
;;
;; Descriptions are capped at 49 characters — see `check-descriptions`, which
;; bb.edn's `examples` task runs so the cap is enforced rather than remembered.
(def examples
  [["basic-controls" "basic-controls" "basics" "button, label and a live click counter"]])

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
