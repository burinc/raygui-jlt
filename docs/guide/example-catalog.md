# The example catalog: 18 raygui demos in jolt

A map of the whole suite. Each example is one namespace under
`src/net/b12n/raygui_jlt/`, runnable by a friendly `bb <name>` task or the underlying
`jolt -M:<alias>`. `scripts/examples_registry.clj` is the single source of truth for
this list; `bb info` prints it live, grouped the same way as below.

```sh
bb <name>       # e.g. bb basic-controls   (opens a window)
bb examples     # flat list with descriptions
bb info         # the grouped cheat-sheet this page mirrors
```

**Current as of writing: 18 examples across 4 groups.** The design targets 24 across
7 groups; dialogs, color and styling (6 more examples) land with a later phase. Run
`bb info` for the count that's actually true right now.

The `shows` column matches the registry description exactly, including its
49-character cap. The `controls` column lists the raygui functions the example
calls through the kwarg API (`rg/<name>!` wrapping `Gui<Name>`), derived by reading
each example's source, not hand-typed from memory.

## basics (4)

| `bb` name | shows | controls exercised |
|---|---|---|
| `basic-controls` | button, label and a live click counter | GuiButton, GuiCheckBox, GuiLabel |
| `icon-buttons` | the embedded 1-bit icon pack on buttons | GuiButton, GuiDrawIcon, GuiLabel, GuiLine, GuiSetIconScale |
| `labels-lines` | labels, separators and a status bar | GuiDummyRec, GuiLabel, GuiLine, GuiSetStyle (text alignment), GuiStatusBar |
| `toggles` | toggle, toggle group and toggle slider | GuiLabel, GuiToggle, GuiToggleGroup, GuiToggleSlider |

## inputs (5)

| `bb` name | shows | controls exercised |
|---|---|---|
| `text-box` | an editable text box with edit mode | GuiLabel, GuiLine, GuiStatusBar, GuiTextBox |
| `text-input-box` | a modal text prompt with secret toggle | GuiButton, GuiLabel, GuiTextInputBox |
| `spinner-value-box` | spinner and value box, clamped and typed | GuiLabel, GuiLine, GuiSpinner, GuiValueBox |
| `sliders` | slider, slider bar and their value cells | GuiLabel, GuiLine, GuiSlider, GuiSliderBar |
| `progress-bar` | a progress bar driven by a timer | GuiLabel, GuiLine, GuiProgressBar |

## collections (5)

| `bb` name | shows | controls exercised |
|---|---|---|
| `dropdown-box` | a dropdown that owns its edit mode | GuiDropdownBox, GuiDummyRec, GuiLabel, GuiStatusBar |
| `combo-box` | a combo box cycling through options | GuiComboBox, GuiLabel, GuiLine |
| `list-view` | a scrollable list with an active row | GuiLabel, GuiListView, GuiStatusBar |
| `list-view-ex` | a list view reporting focus and scroll | GuiLabel, GuiListViewEx, GuiStatusBar |
| `tab-bar` | tabs with close requests | GuiDummyRec, GuiLabel, GuiStatusBar, GuiTabBar |

## containers (4)

| `bb` name | shows | controls exercised |
|---|---|---|
| `panel-group-box` | panels and group boxes as containers | GuiButton, GuiCheckBox, GuiGroupBox, GuiLabel, GuiPanel, GuiSlider, GuiStatusBar |
| `scroll-panel` | a scroll panel over oversized content | GuiLabel, GuiScrollPanel, GuiStatusBar |
| `window-box` | a window box you can close and reopen | GuiButton, GuiCheckBox, GuiLabel, GuiStatusBar, GuiWindowBox |
| `floating-window` | two draggable floating windows | GuiCheckBox, GuiLabel, GuiSlider, GuiStatusBar, GuiWindowBox |

## Not yet built: dialogs, color, styling (6, final phase)

The design targets `message-box` and `custom-input-box` (dialogs), `color-picker`
and `color-picker-hsv` (color), and `style-selector` and `gui-state` (styling). None
of the six exist in the repo yet; they aren't in the table above because this page
lists what `bb info` actually reports, not what the plan promises.

## Ports of raygui's own examples

Four examples are direct Clojure ports of programs in raygui's own `examples/`
tree: `scroll_panel`, `floating_window`, `style_selector` and `custom_input_box`.
Two of the four, `scroll-panel` and `floating-window`, are in the current 18; the
other two land with the final phase. `NOTICE` carries the attribution for all four.

## Adding an example

The five touchpoints, from `AGENTS.md`:

1. **Source**: `src/net/b12n/raygui_jlt/<name_underscored>.clj`
2. **`deps.edn` alias**: `:<name> {:main-opts ["-m" "net.b12n.raygui-jlt.<name>"]}`
3. **`check.clj` require**: alphabetically, so the compile gate covers it
4. **Registry row**: `["<name>" "<alias>" "<group>" "<desc>"]` in
   `scripts/examples_registry.clj`, the single source of truth this page is
   generated from
5. **`bb.edn` task**: `<name> {:doc "▶ <desc>" :task (run-example "<name>")}`

`bb examples` enforces the 49-character description cap and cross-checks the
registry's description against `bb.edn`'s `:doc` string, exiting non-zero on
either mismatch, so a drifted description here is a build failure, not a stale
doc page.

## See also

- [`the-ffi-shape.md`](the-ffi-shape.md): the binding pattern every control in this
  table shares.
- [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md): why a new
  example needs a screenshot that was actually looked at, and in some cases a read
  of the vendored C source, before it's done.
