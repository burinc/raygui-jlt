# raygui-jlt Guide

User-facing documentation for `raygui-jlt`: 18 **[raygui](https://github.com/raysan5/raygui)**
examples written in **[jolt](https://github.com/jolt-lang/jolt)** (native Clojure on
Chez Scheme, no JVM), calling raygui directly over its C ABI through `jolt.ffi`. No
wrapper library, no codegen: one shared bindings namespace and a suite of small
example programs on top of it.

The suite is 18 examples across 4 groups today; a final phase adds 6 more across
dialogs, color and styling for 24 total. `bb info` always prints the live count.

## Why this exists

raygui is raylib's companion immediate-mode GUI library, and it turns out to be a
very good fit for a small, direct FFI binding: its 61 functions almost all share one
shape, a `Rectangle` passed by value, application state through a pointer, an `int`
result. There is no callback machinery, no retained widget tree, and raygui keeps no
per-control state of its own, which is exactly what makes it easy to bind and hard to
leak. [`the-ffi-shape.md`](the-ffi-shape.md) is the full account.

## Relationship to `b12n-raylib-jlt`

Same approach, different library. Both repos bind a real C library directly over its
ABI with `jolt.ffi`, Chez `foreign-procedure` underneath, no wrapper and no codegen.
Where they differ is how hard the library leans on **structs passed by value**, and
that difference is much smaller here than it is there.

raylib's interesting surface is `Image`, `Texture`, `Font`, `Model` and the `Camera2D`
/ `Camera3D` pair, several of which need the pointer-trick workaround `b12n-raylib-jlt`
documents. raygui's only by-value struct anywhere in its control surface is
`Rectangle`, plus `Color` in one icon function and `Font` in the two font accessors
this repo leaves unbound. One recurring 16-byte struct, one module-level scratch
buffer, and the whole class of FFI lifetime bugs a bigger struct surface would invite
never comes up.

## The three commands that matter

```sh
bb lib:build        # compile the vendored header into lib/libraygui.{dylib,so}
bb check             # headless compile of all 18 examples, no window
bb <example-name>    # run one, e.g. bb basic-controls (opens a window)
```

Example tasks build the native library automatically if it is missing, so a fresh
clone runs `bb basic-controls` with no separate setup step.

## What's on these pages

- [`building-libraygui.md`](building-libraygui.md): raygui ships no library. This
  repo builds its own from a vendored header, and the one detail that makes the build
  actually work is that it must link raylib **dynamically**. Read this first if you
  are wondering why there's a build step at all.
- [`the-ffi-shape.md`](the-ffi-shape.md): the recurring by-value-`Rectangle`-in,
  pointer-out, `int`-result signature; the scratch Rectangle and the second buffer
  `GuiScrollPanel` needs; cells and their eight types; the two `char**` functions;
  and what's verified versus assumed on non-AArch64 hardware.
- [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md): four ways an
  example can be wrong while passing every automated check, each one measured, not
  guessed. The most useful page here if you're about to add an example of your own.
- [`example-catalog.md`](example-catalog.md): every example, its group, what it
  shows, and which raygui controls it exercises.

## What this repo does not cover

**Demo GIFs.** Recording is a stated non-goal, not an oversight: the sibling repo
measured that synthetic clicks do not actuate a raylib app at all (0 of 8 clicks
delivered at every tested hold duration), and raygui is mouse-driven by definition,
so a recorded GIF could never show a button being pressed, a dropdown opening, or a
slider being dragged.

## Vendored revision

`vendor/raygui.h` is pinned at **`5.0-9-gfbf5d95`**, zlib licensed. See `NOTICE` for
the full attribution, including the four examples ported from raygui's own example
tree.

## See also

- [raygui](https://github.com/raysan5/raygui): the upstream library.
- [raylib](https://github.com/raysan5/raylib): the library raygui draws through, and
  the one this repo links dynamically at build time.
- [jolt](https://github.com/jolt-lang/jolt): the native Clojure implementation whose
  `jolt.ffi` does all the binding work described on these pages.
- `b12n-raylib-jlt`'s own `docs/guide/`: the sibling this repo's pattern follows,
  and the deeper read on struct-by-value FFI tricks this project mostly doesn't need.
