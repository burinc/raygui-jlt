(ns registration
  "The offline half of the five-touchpoint check.

  CONTRIBUTING.md lists five places one example touches. Two are already
  gated: `bb examples` cross-checks the registry row and the bb.edn task
  :doc against each other, and reports a registry row with no task at all.

  This namespace covers the other three: the source file, the deps.edn
  alias, and the check.clj require.

  Touchpoint 3 is the reason this exists, and CONTRIBUTING.md already calls
  it the trap. `bb check` compiles what check.clj requires, so an example
  left out of that list is never compiled and the run still prints
  \"all example namespaces compiled OK\" and exits 0. Measured 2026-08-29:
  a namespace with a deliberate unresolved symbol fails `bb check` when its
  require is present, and passes when the require is removed. A compile gate
  that goes green over code it never read cannot be the thing that guards
  its own registration."
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def ^:private ns-prefix "net.b12n.raygui-jlt.")

;; Required by check.clj but not examples, so neither missing rows nor
;; orphaned sources.
(def ^:private shared-namespaces #{"check" "raygui" "raylib"})

(defn source-path
  "Where an example's source must live. Clojure's underscore rule applies, so
  a hyphenated name maps to an underscored file."
  [root nm]
  (str (fs/path root "src" "net" "b12n" "raygui_jlt"
                (str (str/replace nm "-" "_") ".clj"))))

(defn- read-forms
  "Every top-level form in a file, read to EOF.

  `read-string` would stop after the first form. That happens to be the ns
  form here, so it would work today and quietly stop working the moment
  check.clj grows a second form worth reading. Reading to EOF costs nothing.
  EDN is not an option: source files carry reader macros EDN has no notion
  of."
  [path]
  (let [r (java.io.PushbackReader. (java.io.StringReader. (slurp path)))]
    (loop [forms []]
      (let [f (read {:read-cond :allow
                     :eof ::eof} r)]
        (if (= f ::eof) forms (recur (conj forms f)))))))

(defn alias-targets
  "The deps.edn :aliases, as {alias-name main-namespace-string}.

  The namespace is carried through rather than discarded, so an alias
  copy-pasted from its neighbour and left pointing at the wrong example can
  be reported. That one runs the wrong program rather than failing."
  [root]
  (let [aliases (:aliases (edn/read-string (slurp (str (fs/path root "deps.edn")))))]
    (into {} (map (fn [[k v]]
                    [(name k) (second (:main-opts v))])
                  aliases))))

(defn check-requires
  "The example names required by check.clj."
  [root]
  (let [forms   (read-forms (source-path root "check"))
        ns-form (first (filter (fn [f] (and (seq? f) (= 'ns (first f)))) forms))]
    (->> ns-form
         (filter (fn [x] (and (seq? x) (= :require (first x)))))
         first rest
         (map first)
         (keep (fn [s]
                 (let [n (str s)]
                   (when (str/starts-with? n ns-prefix)
                     (subs n (count ns-prefix))))))
         set)))

(defn problems
  "Every registration gap, as [name explanation] pairs. Empty means clean."
  [root examples]
  (let [aliases  (alias-targets root)
        requires (check-requires root)
        gaps
        (mapcat
         (fn [row]
           (let [nm (nth row 0)
                 al (nth row 1)
                 want (str ns-prefix nm)]
             (cond-> []
               (not (fs/exists? (source-path root nm)))
               (conj [nm (str "no source at src/net/b12n/raygui_jlt/"
                              (str/replace nm "-" "_") ".clj")])

               (not (contains? aliases al))
               (conj [nm (str "no :" al " alias in deps.edn, so jolt -M:" al
                              " cannot run it")])

               (and (contains? aliases al)
                    (not= want (get aliases al)))
               (conj [nm (str "deps.edn alias :" al " runs " (get aliases al)
                              ", expected " want)])

               (not (contains? requires nm))
               (conj [nm (str "missing from check.clj :require, so bb check "
                              "never compiles it and still reports success")]))))
         examples)
        registered (set (map (fn [r] (nth r 0)) examples))
        orphans
        (->> (fs/glob (fs/path root "src") "**/*.clj")
             (map (fn [p] (str/replace (str (fs/file-name p)) ".clj" "")))
             (map (fn [f] (str/replace f "_" "-")))
             (remove shared-namespaces)
             (remove registered)
             (map (fn [f] [f "a source file with no row in scripts/examples_registry.clj"])))]
    (concat gaps orphans)))
