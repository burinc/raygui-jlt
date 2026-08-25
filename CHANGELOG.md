# Changelog

Notable changes to raygui-jlt, newest first. The format follows
[babashka's changelog](https://github.com/babashka/babashka/blob/master/CHANGELOG.md):
one bullet per user-visible change, written as what a reader would notice
rather than what a commit did.

Examples read at <https://raygui-jlt.b12n.app>.

## 0.1.0 - Unreleased

The suite is complete: 24 examples across 7 groups.

- [raygui](https://github.com/raysan5/raygui), raylib's immediate-mode GUI
  library, ported to [jolt](https://github.com/jolt-lang/jolt), a native
  Clojure on Chez Scheme with no JVM: 24 example programs calling raygui
  directly over its C ABI through `jolt.ffi`, no wrapper library, no codegen.
- **raygui is header-only, so this repo builds its own library.** There is
  no `libraygui` to install and no Homebrew formula for it. `vendor/raygui.h`
  is a pinned, unmodified copy of upstream's header at revision
  `5.0-9-gfbf5d95` (see `NOTICE`), and `bb lib:build` compiles
  `vendor/raygui_impl.c` into `lib/libraygui.dylib`. A fresh clone needs no
  separate setup step: every example task builds the library first if it is
  missing.
- **Two Clojure layers.** `net.b12n.raygui-jlt.raygui` holds the raw
  `jolt.ffi` bindings plus a keyword-argument control API on top of them
  (`rg/slider!` and friends); `net.b12n.raygui-jlt.raylib` is a trimmed
  derivative of the bindings in the sibling project `b12n-raylib-jlt`,
  carrying only the window, frame, input, colour and text surface these
  examples need to host raygui.
- **The gates**: `bb check` (headless compile of every example, no window),
  `bb lint`, `bb examples` (which also enforces a 49-character description
  cap and a registry/`bb.edn` `:doc` drift check), a pre-commit hook running
  lint plus format plus clean-ns, and a per-example screenshot, the one gate
  that catches what the other four cannot.
- **Four measured traps, documented so the next reader inherits them instead
  of re-finding them:**
  1. `TakeScreenshot` without a prior `rlDrawRenderBatchActive` writes a
     blank frame. raylib defers batched geometry until `EndDrawing`, so a
     mid-frame screenshot captures the framebuffer as it stood before the
     current frame's drawing landed.
  2. raygui packs style colours as `0xRRGGBBAA`; raylib's `Color` packs
     `0xAABBGGRR`. Feeding one straight into a function expecting the other
     swaps red and blue into a plausible, wrong colour rather than erroring.
     Measured on the `cyber` style's `LINE_COLOR` (`0x81C0D0FF`, a light
     blue): fed raw to `ClearBackground` it renders salmon pink instead.
  3. `ffi/defcfn` binds lazily. A misspelled C symbol loads with no error at
     all and throws only the first time the function is actually called.
     `bb check` requires every example namespace but never calls a single
     binding, so it cannot see this class of bug by construction.
  4. Outcome logic is invisible to a screenshot. `GuiTextInputBox` and
     `GuiMessageBox` report which button was pressed by writing `0` for the
     window's own close icon and, for every button in the caller's list,
     `i + 1`: one-based, not zero-based. A screenshot shows the dialog open;
     it never shows what its buttons actually report.
- `GuiSetFont`/`GuiGetFont` are deliberately left unbound, 59 of raygui's 61
  functions bound rather than all of them: the vendored `.rgs` styles each
  carry and apply their own embedded font, so no example needs a font
  accessor, and binding one would add a marshalling layer nothing calls.
