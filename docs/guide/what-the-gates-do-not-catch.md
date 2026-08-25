# What the gates do not catch

The most useful page in this guide. Four failure classes, each found by running
something and being surprised, each of which passes at least one automated gate
while rendering or behaving wrong. For a GUI toolkit, that combination is the normal
case, not the exception, which is why the screenshot is treated as a required gate
here rather than a nicety.

## 1. A mid-frame screenshot without the batch flush writes a blank frame

raylib defers batched geometry, `DrawText`, shapes, everything, until `EndDrawing`.
Calling `TakeScreenshot` before that flush captures the framebuffer as it stood
**before** the current frame's drawing landed: a perfectly blank, valid PNG from a
program that just drew four controls.

`maybe-screenshot!` flushes first, which is why every example's screenshot works:

```clojure
(ffi/defcfn ^:private flush-batch "rlDrawRenderBatchActive" [] :void)

(defn maybe-screenshot! [frame at]
  (when (and shot-path (= frame at))
    (flush-batch)
    (take-screenshot shot-path)
    ...))
```

**bb check does not catch this.** It compiles every example headlessly and never
draws a frame at all. **The gate that catches it is looking at the PNG**: a blank
image means the flush is missing (or never ran), not that the example drew nothing
by design.

## 2. Style colours are byte-swapped relative to raylib `Color`

raygui stores style colours as `0xRRGGBBAA`. raylib's `Color` packs little-endian as
`0xAABBGGRR`. Feed one straight into a function expecting the other and you get a
**plausible, wrong** colour, not an error.

Measured on the `cyber` style's background:

```
GuiGetStyle(DEFAULT, BACKGROUND_COLOR)  ->  0x81C0D0FF   (R=129 G=192 B=208 A=255, light blue)
fed directly to ClearBackground         ->  renders salmon pink
via GetColor()                          ->  renders light blue, correct
```

Nothing here throws. The window opens, the background paints some colour, and only
comparing the render against the style's own declared value shows it's the wrong
one, red and blue swapped into something that still looks like a deliberate choice.
`style-color` wraps `GetColor` so this conversion happens once, correctly, and every
example that themes itself from the loaded style goes through it rather than reading
`GuiGetStyle` raw.

**bb check does not catch this** (no color comparison happens headlessly), and
neither does a glance at the screenshot alone, since the wrong colour is still a
colour. **The gate is checking the rendered colour against the style's own declared
value**, not just confirming that something painted.

## 3. `ffi/defcfn` binds lazily

A `jolt.ffi/defcfn` form loads with **no error** even when the C symbol name it
names doesn't exist. The failure only appears the first time the function is
**called**:

```
Exception in foreign-procedure: no entry for "GuiGetStyleTYPO"
```

`bb check` compiles and requires every example namespace, which resolves macros and
vars but never calls a single `gui-*` binding. A misspelled C symbol therefore
compiles clean, passes `bb check`, and throws only at run time, inside whatever
example first calls it, on whichever machine happens to run it next.

**bb check does not catch this by construction**: it is a require, not a call.
**The gate is either actually running the example** (so the call happens and the
missing symbol throws), **or a static cross-check** of every `ffi/defcfn` symbol
name against `nm -gU` on the built library, confirming each named C symbol exists
before anything tries to call it.

## 4. Outcome logic is invisible to screenshots

The sharpest of the four, because the first three are all visible-if-you-know-to-look:
a blank frame, a wrong colour, a thrown exception. This one produces a screenshot
that looks completely correct while the code behind it does the opposite of what it
claims.

**A screenshot shows a dialog open. It never shows what its buttons do.**

The real example: `GuiTextInputBox` reports which button was pressed by writing an
`int` result that is `0` for the window's own close **X**, and **one-based** for
every button in the caller's semicolon list otherwise:

```c
// raygui.h: btnActive = 0 for the window X, i+1 for the i-th button
```

With `:buttons "Cancel;OK"` that makes Cancel `1` and OK `2`. `text-input-box.clj`
first checked `(= 1 result)` to detect "OK pressed". Nothing about that crashes or
even looks wrong on screen: the dialog opens, a button click closes it, some text
appears. It just reports the **wrong button every time**, Cancel silently reporting
success and OK silently reporting cancellation, with the on-screen result looking
exactly as intended either way. Fixed by checking `(= 2 result)` for OK instead, per
the header's actual encoding.

A second instance in the same suite: `tab-bar.clj` told a reader to click a tab's
close **x**. `GuiTabBarEx` gates the entire close-button block on
`GuiGetStyle(TABBAR, TAB_CLOSE_BUTTON)` (raygui.h:3938), and `GuiLoadStyleDefault`
never sets that flag, leaving it `0`. The close button the instructions pointed at
**was never drawn**. The tab bar itself rendered fine, tabs selected correctly, and
nothing about the screenshot showed a missing button, because there was nothing
there to be missing from the image. Fixed with one line before the frame loop:

```clojure
;; The close 'x' is off by default: GuiTabBarEx gates it on this style flag and
;; GuiLoadStyleDefault never sets it, so without this line the instruction
;; below points at a button that is never drawn.
(rg/gui-set-style rg/TABBAR rg/TAB-CLOSE-BUTTON 1)
```

**No automated gate catches this class, and neither does looking at the PNG.** The
only thing that catches an outcome-logic bug is **reading the vendored C source**
for any control whose result encodes more than a plain "pressed / not pressed": what
each return value actually means, and which style flags gate which visible pieces.

## Summary: which tool catches which class

| failure | `bb check` | screenshot | what actually catches it |
|---|---|---|---|
| missing batch flush (blank frame) | no | **yes** | look at the PNG |
| byte-swapped style colour | no | not alone | compare the rendered colour to the style's declared value |
| misspelled C symbol (lazy `defcfn`) | no | no | run the example, or cross-check symbol names against `nm -gU` |
| wrong outcome/result encoding | no | no | read the vendored C source |

The first two are why the screenshot step exists at all for this suite. The last two
are why it isn't sufficient by itself, and why every example task in this project's
plan also asks whether the control's result meaning was checked against the header,
not just whether the control renders.

## See also

- [`building-libraygui.md`](building-libraygui.md): the batch-flush and byte-swap
  traps both sit downstream of a correctly built, dynamically-linked library.
- [`the-ffi-shape.md`](the-ffi-shape.md): the binding shapes these failures hide
  inside.
