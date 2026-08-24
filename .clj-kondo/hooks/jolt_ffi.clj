(ns hooks.jolt-ffi
  "clj-kondo hook for jolt.ffi/defcfn.

  `defcfn` binds a C symbol to a Clojure var:

      (ffi/defcfn gui-button \"GuiButton\" [...] :int)

  clj-kondo cannot see through the macro, so without this hook every bound name
  is an `Unresolved symbol` inside raygui.clj and an `Unresolved var: rg/…` at
  every call site in the examples — enough noise to make the linter useless.

  The hook rewrites the form into a `defn` of the same name whose parameter count
  matches the C argument-type vector, and whose body is a literal of the declared
  C return type. That buys three things clj-kondo could not otherwise know:

    * the var exists (kills the false positives),
    * its arity — passing the wrong number of arguments to a binding is exactly
      the FFI mistake that otherwise surfaces only as a native crash,
    * its return type.

  Two shapes carry an argument the C signature does not show:

    * a `[:by-value [:struct …]]` RETURN. jolt writes the struct through a
      caller-supplied destination pointer passed as the FIRST argument, so the
      Clojure arity is one MORE than the argtype vector. (No binding in this
      project returns an aggregate today; the case is handled so that adding one
      does not silently produce a wrong arity.)
    * a `:varargs` marker inside the argtype vector, which is a boundary marker
      rather than a parameter, so the arity is one LESS than the vector's length.

  Note that a `[:by-value …]` ARGUMENT is an ordinary parameter and counts
  normally — which is the common case here, since every raygui control leads with
  a by-value Rectangle."
  (:require [clj-kondo.hooks-api :as api]))

(def ^:private numeric-ret
  #{:int :uint :long :ulong :short :ushort :byte :ubyte :float :double :size-t})

(defn- ret-node
  "A literal whose inferred type matches the declared C return type."
  [ret]
  (let [k (when ret (api/sexpr ret))]
    (cond
      (contains? numeric-ret k) (api/token-node 0)
      (= :string k)             (api/string-node "")
      :else                     (api/token-node nil))))

(defn- aggregate?
  "A [:by-value [:struct …]] type node."
  [n]
  (and n (api/vector-node? n)
       (= :by-value (some-> n :children first api/sexpr))))

(defn- varargs-marker?
  [n]
  (= :varargs (api/sexpr n)))

(defn defcfn
  [{:keys [node]}]
  (let [[_defcfn name-node _c-symbol arg-types ret] (:children node)]
    ;; Only rewrite the shape we understand; anything else falls through to the
    ;; default analysis rather than silently interning a wrong var.
    (if (and name-node arg-types (api/vector-node? arg-types))
      (let [arg-count (count (remove varargs-marker? (:children arg-types)))
            n (cond-> arg-count (aggregate? ret) inc)
            params (map (fn [i] (api/token-node (symbol (str "_arg" i)))) (range n))
            expanded (api/list-node
                      [(api/token-node 'clojure.core/defn)
                       name-node
                       (api/vector-node (vec params))
                       (ret-node ret)])]
        {:node (with-meta expanded (meta node))})
      {:node node})))
