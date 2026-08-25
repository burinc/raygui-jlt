# The example catalog

A map of the whole suite. Each example is one namespace under
`src/net/b12n/raygui_jlt/`, runnable with a friendly `bb <name>` task or the
underlying `jolt -M:<name>` without babashka. `scripts/examples_registry.clj`
is the single source of truth this page is generated from; `bb info` prints
the same grouping live.

```sh
bb <name>       # e.g. bb basic-controls (opens a window)
bb examples     # flat list with descriptions, plus the count
bb info         # the grouped cheat-sheet this page mirrors
```

**18 examples across 4 groups today.** The suite is still growing: a further
6 are planned across dialogs, color and styling, three groups that do not
have a single example in them yet. Run `bb info` for the count that is
actually true right now, not this page's memory of it.

## basics

| preview | `bb` name | what it shows |
|---|---|---|
| [<img src="../demos/basic-controls.png" width="180">](../demos/basic-controls.png) | `basic-controls` | button, label and a live click counter |
| [<img src="../demos/icon-buttons.png" width="180">](../demos/icon-buttons.png) | `icon-buttons` | the embedded 1-bit icon pack on buttons |
| [<img src="../demos/labels-lines.png" width="180">](../demos/labels-lines.png) | `labels-lines` | labels, separators and a status bar |
| [<img src="../demos/toggles.png" width="180">](../demos/toggles.png) | `toggles` | toggle, toggle group and toggle slider |

## inputs

| preview | `bb` name | what it shows |
|---|---|---|
| [<img src="../demos/text-box.png" width="180">](../demos/text-box.png) | `text-box` | an editable text box with edit mode |
| [<img src="../demos/text-input-box.png" width="180">](../demos/text-input-box.png) | `text-input-box` | a modal text prompt with secret toggle |
| [<img src="../demos/spinner-value-box.png" width="180">](../demos/spinner-value-box.png) | `spinner-value-box` | spinner and value box, clamped and typed |
| [<img src="../demos/sliders.png" width="180">](../demos/sliders.png) | `sliders` | slider, slider bar and their value cells |
| [<img src="../demos/progress-bar.png" width="180">](../demos/progress-bar.png) | `progress-bar` | a progress bar driven by a timer |

## collections

| preview | `bb` name | what it shows |
|---|---|---|
| [<img src="../demos/dropdown-box.png" width="180">](../demos/dropdown-box.png) | `dropdown-box` | a dropdown that owns its edit mode |
| [<img src="../demos/combo-box.png" width="180">](../demos/combo-box.png) | `combo-box` | a combo box cycling through options |
| [<img src="../demos/list-view.png" width="180">](../demos/list-view.png) | `list-view` | a scrollable list with an active row |
| [<img src="../demos/list-view-ex.png" width="180">](../demos/list-view-ex.png) | `list-view-ex` | a list view reporting focus and scroll |
| [<img src="../demos/tab-bar.png" width="180">](../demos/tab-bar.png) | `tab-bar` | tabs with close requests |

## containers

| preview | `bb` name | what it shows |
|---|---|---|
| [<img src="../demos/panel-group-box.png" width="180">](../demos/panel-group-box.png) | `panel-group-box` | panels and group boxes as containers |
| [<img src="../demos/scroll-panel.png" width="180">](../demos/scroll-panel.png) | `scroll-panel` | a scroll panel over oversized content. Port of raygui's own `scroll_panel` example. |
| [<img src="../demos/window-box.png" width="180">](../demos/window-box.png) | `window-box` | a window box you can close and reopen |
| [<img src="../demos/floating-window.png" width="180">](../demos/floating-window.png) | `floating-window` | two draggable floating windows. Port of raygui's own `floating_window` example. |

## What the previews are

Every preview is a real capture of the example actually running, not a
mockup. Each was produced by running the example with
`RAYGUI_APP_AUTO_QUIT_MS` set, so the window closes itself, and
`RAYGUI_APP_SHOT` pointed at a path, so it screenshots frame 30 before it
does: the same mechanism [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md)
describes as this project's actual gate.

They are **static frames, not recordings**, and that is a deliberate choice,
not a shortcut. raygui is mouse-driven by definition, and synthetic input
does not actuate a raylib/GLFW window at all: measured upstream, 0 of 8
synthetic clicks and 0 of 2 synthetic drags were delivered at any hold
duration tried, against 3 of 3 for plain pointer motion, because a window
like this ignores pid-routed button events. An animated recording could show
a cursor drifting toward a button; it could never show that button actually
being pressed, a dropdown opening, or a slider being dragged. A still frame
does not claim more than it shows, which is the honest version of a preview
here.

## Not yet built

A further 6 examples are planned, spread across three groups this project
does not have a single example in yet: dialogs, color and styling. None of
the six exist in the repo today, so they are not in the tables above. This
page lists what `bb info` actually reports, not what a plan promises.

## Ports of raygui's own examples

Four examples are direct ports of programs in raygui's own `examples/` tree:
`scroll-panel` and `floating-window`, both built and in the tables above,
plus `custom-input-box` and `style-selector`, both still planned. `NOTICE`
carries the attribution for all four.

## Adding an example

Five touchpoints, from `AGENTS.md`:

1. **Source**: `src/net/b12n/raygui_jlt/<name_underscored>.clj`
2. **`deps.edn` alias**: `:<name> {:main-opts ["-m" "net.b12n.raygui-jlt.<name>"]}`
3. **`check.clj` require**: alphabetically, so the compile gate covers it
4. **Registry row**: `["<name>" "<alias>" "<group>" "<desc>"]` in
   `scripts/examples_registry.clj`, the single source of truth this page is
   generated from
5. **`bb.edn` task**: `<name> {:doc "▶ <desc>" :task (run-example "<name>")}`

`bb examples` enforces the 49-character description cap and cross-checks the
registry's description against `bb.edn`'s `:doc` string, exiting non-zero on
either mismatch, so a drifted description here is a build failure, not a
stale doc page.

## See also

- [`the-ffi-shape.md`](the-ffi-shape.md): the binding pattern every control in
  this catalog shares.
- [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md): why a
  new example needs a screenshot that was actually looked at, and in some
  cases a read of the vendored C source, before it is done.
