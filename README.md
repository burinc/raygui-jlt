# raygui-jlt

24 [raygui](https://github.com/raysan5/raygui) examples written in
[jolt](https://github.com/jolt-lang/jolt) (native Clojure on Chez Scheme, no JVM),
calling raygui directly over its C ABI through `jolt.ffi`. No wrapper library, no
codegen: one shared bindings namespace and a suite of small example programs on top
of it. raygui draws through [raylib](https://github.com/raysan5/raylib), so a small
raylib bindings layer hosts the window and the frame loop underneath every example.

## Quick start

```sh
brew install raylib   # macOS; see docs/guide/building-libraygui.md for other platforms
bb lib:build           # compile the vendored raygui header into lib/libraygui.dylib
bb basic-controls      # run the smallest example (opens a window)
```

Every example task builds the native library automatically if it is missing, so in
practice `bb basic-controls` alone is enough on a fresh clone: it runs `bb lib:build`
for you the first time.

## Why there is a build step at all

Unlike raylib, raygui is header-only: there is no `libraygui` to install and no
Homebrew formula for it. This repo vendors `vendor/raygui.h` and compiles it into
`lib/libraygui.dylib` (`lib/libraygui.so` on Linux) so that `jolt.ffi` has a shared
library to load.

## The examples

The suite is **24 examples across 7 groups**. This table is generated from `bb info`,
so it can't drift from the registry; run `bb info` yourself for the live, grouped
version, or `bb examples` for a flat list.

| Group | Task | What it shows |
|---|---|---|
| basics | `bb basic-controls` | button, label and a live click counter |
| basics | `bb icon-buttons` | the embedded 1-bit icon pack on buttons |
| basics | `bb labels-lines` | labels, separators and a status bar |
| basics | `bb toggles` | toggle, toggle group and toggle slider |
| inputs | `bb text-box` | an editable text box with edit mode |
| inputs | `bb text-input-box` | a modal text prompt with secret toggle |
| inputs | `bb spinner-value-box` | spinner and value box, clamped and typed |
| inputs | `bb sliders` | slider, slider bar and their value cells |
| inputs | `bb progress-bar` | a progress bar driven by a timer |
| collections | `bb dropdown-box` | a dropdown that owns its edit mode |
| collections | `bb combo-box` | a combo box cycling through options |
| collections | `bb list-view` | a scrollable list with an active row |
| collections | `bb list-view-ex` | a list view reporting focus and scroll |
| collections | `bb tab-bar` | tabs with close requests |
| containers | `bb panel-group-box` | panels and group boxes as containers |
| containers | `bb scroll-panel` | a scroll panel over oversized content |
| containers | `bb window-box` | a window box you can close and reopen |
| containers | `bb floating-window` | two draggable floating windows |
| dialogs | `bb message-box` | a modal message box with two buttons |
| dialogs | `bb custom-input-box` | a hand-built input dialog over a panel |
| color | `bb color-picker` | an RGB picker with an alpha bar |
| color | `bb color-picker-hsv` | the HSV picker and panel |
| styling | `bb style-selector` | cycle six vendored .rgs style themes |
| styling | `bb gui-state` | forced states, alpha and lock |

The suite is complete: all 24 planned examples are built.

## How it works

raygui's API is 61 functions, and nearly all of them share one shape: a bounding
`Rectangle` passed **by value**, application state passed through a **pointer**,
and an **`int`** result telling the caller what happened. That one recurring
signature is what makes a small, direct FFI binding practical here, with no
callback machinery and no retained widget tree to model. See
[`docs/guide/the-ffi-shape.md`](docs/guide/the-ffi-shape.md) for the full account,
including the scratch-buffer trick that keeps every control from allocating inside
a frame.

## Verifying without a display

Every example honors two environment variables so it can prove itself with nobody
watching:

```sh
RAYGUI_APP_AUTO_QUIT_MS=1500 jolt -M:basic-controls   # close the window after 1.5s
RAYGUI_APP_SHOT=proof.png    jolt -M:basic-controls   # dump one frame as a PNG
```

The shot path must be **relative**: raylib prepends the working directory to it,
so an absolute path writes nothing. The helper verifies the file appeared and
prints `SHOT FAILED` rather than reporting a success it did not have.

**The PNG is the gate.** For a GUI toolkit, a headless compile check proves the code
loads, not that it renders correctly; several traps documented in the guide render a
plausible, wrong result rather than throwing. Looking at the screenshot is what
actually catches them.

## Documentation

- Guide: [`docs/guide/`](docs/guide/index.md), starting with
  [`index.md`](docs/guide/index.md).
- Site: <https://raygui-jlt.b12n.app>, the same content published and cross-linked.

## License and attribution

Released under the **zlib/libpng license**; see [`LICENSE`](LICENSE). Third-party
attribution, including the vendored raygui header and the ported examples, lives in
[`NOTICE`](NOTICE).
