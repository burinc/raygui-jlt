# The FFI shape

Why raygui suits jolt, for a reader who knows neither.

## One recurring signature

raygui's API is 61 functions, and nearly every control shares the same three-part
shape: a bounding `Rectangle` **by value**, application state through a **pointer**,
and an **`int`** result telling the caller what happened.

```c
int GuiSlider(Rectangle bounds, const char *textLeft, const char *textRight,
              float *value, float minValue, float maxValue);
```

The raw binding mirrors it directly:

```clojure
(ffi/defcfn gui-slider "GuiSlider"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]]
   :string :string :pointer :float :float] :int)
```

and the kwarg wrapper on top hides the bounds construction and turns the raw result
into the shape a caller actually wants:

```clojure
(defn slider!
  [& {:keys [x y w h left right cell min max]
      :or {x 0 y 0 w 200 h 20 left "" right "" min 0.0 max 1.0}}]
  (pos? (gui-slider (bounds! x y w h) left right (ptr cell)
                    (double min) (double max))))
```

`bounds!` is the scratch Rectangle below; `cell` is the pointer out-param below;
`pos?` turns raygui's `RESULT_CHANGED` (`2`) into a plain boolean, true on change.
That's the whole pattern, repeated with minor variation across most of the 61
functions. There is no callback machinery to bind, no retained widget tree to model,
and raygui keeps no per-control state of its own to worry about ownership of.

## The single scratch Rectangle

Every control takes `Rectangle bounds` by value, and jolt's `[:by-value ...]` wants a
pointer to caller-owned native storage, **copying** the struct's bytes at call time
rather than retaining the pointer. That last fact is what makes one shared buffer
safe: since the callee never holds on to the pointer past the call, the caller can
reuse the same 16 bytes for every control in the frame.

Verified, not assumed: four controls drawn at four different positions through one
shared buffer render pixel-identical to four separate allocations. So the raygui
namespace owns exactly one module-level scratch `Rectangle`, rewritten immediately
before each control call:

```clojure
(def ^:private scratch-rect (ffi/alloc (ffi/layout-size rect-layout)))

(defn bounds! [x y w h] (write-rect! scratch-rect x y w h))
```

The consequence is the point, not a micro-optimisation: **no example allocates or
frees a `Rectangle` inside a frame**, which removes an entire class of FFI lifetime
bugs from all 18 (soon 24) example programs. The only lifetimes an example manages
are its cells, below.

## The second buffer, for `GuiScrollPanel`

One function breaks the one-buffer rule: `GuiScrollPanel` is the only raygui
function that takes **two** by-value `Rectangle`s in a single call, its own bounds
and the size of the content behind it.

```c
int GuiScrollPanel(Rectangle bounds, const char *text, Rectangle content,
                    Vector2 *scroll, Rectangle *view);
```

Clojure evaluates arguments left to right. Writing both rectangles through the same
`bounds!` call would let the second write clobber the first before the FFI call ever
happens, handing raygui the same rectangle for both parameters.

Measured, in the actual `scroll-panel` example: with `content!` not yet in place, a
panel declared at **300×200** rendered at its **560×420** content size, covering the
window, with both scrollbars live and no error anywhere, on screen or in a log. It
looked like a working, if oddly large, scroll panel. Only the on-screen dimensions
gave it away.

The fix is a second, dedicated scratch buffer:

```clojure
(def ^:private scratch-content (ffi/alloc (ffi/layout-size rect-layout)))
(defn content! [x y w h] (write-rect! scratch-content x y w h))

(defn scroll-panel! [& {:keys [x y w h text content-w content-h scroll view] ...}]
  (gui-scroll-panel (bounds! x y w h) text
                    (content! 0 0 content-w content-h)
                    (ptr scroll) (ptr view)))
```

There is no third case: no other raygui function in the control surface takes more
than one by-value struct, so two buffers is the whole fix, not a pattern to extend.

## Cells: the pointer out-params

raygui keeps no state of its own. The application owns every value a control reads
or writes, and C wants a pointer to it. A **cell** is a typed native slot, allocated
once outside the frame loop and freed once after it:

```clojure
(let [vol (rg/cell :float 0.35)]
  (rg/slider! :x 90 :y 130 :w 200 :h 20 :cell vol :min 0.0 :max 1.0)
  (rg/value vol))                       ; => 0.35, read with the right type
```

Eight cell types are supported: `:float`, `:int`, `:bool`, `:color`, `:vector2`,
`:vector3`, `:rect` (all allocated through `cell`), and `:text` (allocated through
the separate `text-cell`, because a text buffer's size isn't implied by its type the
way the others' are). `value` reads a cell back typed; `reset-cell!` writes one;
`free-cell!` releases it and is idempotent, since freeing an already-freed cell
aborts the process outright rather than raising a catchable exception.

The raw `gui-*` bindings stay public underneath the kwarg layer, so an example can
drop to the plain C shape where the wrapper gets in the way.

## The two `char**` functions

Most list-shaped controls take their options as **one semicolon-separated string**
and answer with an index in an `:int` cell, which keeps arrays off the FFI boundary
entirely. Two functions break that pattern and take a real `const char **` array:
`GuiListViewEx` and `GuiTabBarEx`. Both are bound raw; only `list-view-ex!` wraps its
`Ex` variant, built and freed inside one `ffi/with-c-string-array` so the array's
member pointers never outlive the call that uses them:

```clojure
(let [items (vec items) n (count items)]
  (ffi/with-c-string-array [arr n] items
    (pos? (gui-list-view-ex (bounds! x y w h) arr n
                            (ptr scroll) (ptr cell) (ptr focus)))))
```

`tab-bar!` deliberately calls the plain `GuiTabBar` (the semicolon-string variant)
rather than the bound `GuiTabBarEx`, keeping the char** path to the one example that
needs it.

## What's verified on AArch64, and what isn't

`Rectangle` is 16 bytes of homogeneous float, small enough that AArch64 passes it in
registers rather than indirectly through a pointer, a different path from
`b12n-raylib-jlt`'s pointer trick for its 24-byte `Camera2D` (too large for the
register path, faked with a hand-built pointer instead). jolt 0.7.23's
`[:by-value [:struct ...]]` handles the register-passed case without the caller
knowing the difference.

This has been driven from jolt against a real window and verified by screenshot **on
AArch64 only**. SysV x86-64 classifies a 16-byte homogeneous-float aggregate into a
different register class, and nothing in this repo has run there. State what was
tested; do not assume it ports.

## Why 59 of the 61 are bound

Two functions, `GuiSetFont` and `GuiGetFont`, pass raylib's `Font` struct by value
and are **left unbound**, not bound-and-unused. The vendored `.rgs` styles each carry
and apply their own embedded font on load, so no example needs a font accessor, and
binding one would add a `Font` marshalling layer nothing calls.

## See also

- [`building-libraygui.md`](building-libraygui.md): the library these bindings load.
- [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md): four ways code
  built on this shape can be wrong and still pass every automated check.
