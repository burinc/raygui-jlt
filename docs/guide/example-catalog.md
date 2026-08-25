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

| preview | `bb` name | what it demonstrates |
|---|---|---|
| [<img src="../demos/basic-controls.png" width="180">](../demos/basic-controls.png) | `basic-controls` | The smallest complete raygui program: a button, a label and a checkbox over a live click counter. It proves the vendored library loaded, the by-value Rectangle reached the right bounds, and a `:bool` cell round-tripped through raygui's pointer API. |
| [<img src="../demos/icon-buttons.png" width="180">](../demos/icon-buttons.png) | `icon-buttons` | raygui ships 256 icons inside the header itself, so there is no image file to load. An icon reaches a control through its text: `GuiIconText` prepends a `#nnn#` marker that any control renders as the icon, while `GuiDrawIcon` draws one directly at a pixel size. The last row sets icon scale to 2, a reminder that the scale is global and persists until it is set back. |
| [<img src="../demos/labels-lines.png" width="180">](../demos/labels-lines.png) | `labels-lines` | The controls that carry no state: labels at three text alignments, separators with and without a caption, a placeholder box and a status bar. Also the one thing to watch about raygui styling: style properties are global and persist across frames, so a control that changes `TEXT_ALIGNMENT` for itself has to restore it, or every later control inherits it. |
| [<img src="../demos/toggles.png" width="180">](../demos/toggles.png) | `toggles` | The three toggle controls side by side. `GuiToggle` is a single on/off button over a `:bool` cell; `GuiToggleGroup` and `GuiToggleSlider` both take their options as one semicolon-separated string and report the selected index through an `:int` cell, raygui's usual way of passing a list without an array crossing the FFI boundary. |

## inputs

| preview | `bb` name | what it demonstrates |
|---|---|---|
| [<img src="../demos/text-box.png" width="180">](../demos/text-box.png) | `text-box` | An editable text box, and the edit-mode pattern every text-entry control in raygui uses. raygui keeps no memory of which box is being edited: the control returns non-zero when it wants the mode toggled, and the application flips its own `:bool` cell, so the same mechanism that carries control values also carries UI state. The buffer itself is a `:text` cell, a fixed char array raygui edits in place. |
| [<img src="../demos/text-input-box.png" width="180">](../demos/text-input-box.png) | `text-input-box` | `GuiTextInputBox`, a modal prompt built from a title, a message, an entry and a button row. The button row is one semicolon-separated string and the result arrives as an index in an `:int` cell, exactly like the toggle group. The optional secret cell adds a show/hide toggle and masks the entry, which is why it is a cell rather than a plain flag. |
| [<img src="../demos/spinner-value-box.png" width="180">](../demos/spinner-value-box.png) | `spinner-value-box` | The two integer entry controls. `GuiSpinner` has increment and decrement buttons; `GuiValueBox` is the same field without them, for typing a number directly. Both clamp to their own min and max once editing ends, but not while typing, so the cell can transiently hold an out-of-range value mid-edit. Both carry the edit-mode pattern from `text-box`. |
| [<img src="../demos/sliders.png" width="180">](../demos/sliders.png) | `sliders` | `GuiSlider` and `GuiSliderBar` over `:float` cells. The two differ only in appearance: the slider draws a handle on a plain track, the slider bar fills the track up to the value. Both write straight into a `:float` cell, which is the whole of their state. The bottom row feeds a slider's value into a raylib circle, showing that a control's value is just a number once it is read back. |
| [<img src="../demos/progress-bar.png" width="180">](../demos/progress-bar.png) | `progress-bar` | `GuiProgressBar`, the one control the user cannot move. It still takes its value by pointer like every other control, because raygui's API is uniform, but nothing in the control writes back: the application advances the cell. Here a timer does it, wrapping at 100%, with a second bar showing the same value against a different range. |

## collections

| preview | `bb` name | what it demonstrates |
|---|---|---|
| [<img src="../demos/dropdown-box.png" width="180">](../demos/dropdown-box.png) | `dropdown-box` | `GuiDropdownBox`, and the one layout rule immediate mode imposes. An open dropdown paints its list outside its own bounds, and raygui has no z-order, so draw order is paint order: a dropdown has to be drawn last, or whatever comes after it paints over the open list. This example draws its content first and the two dropdowns last, deliberately. |
| [<img src="../demos/combo-box.png" width="180">](../demos/combo-box.png) | `combo-box` | `GuiComboBox`, the dropdown's simpler sibling. It never opens a list: a click just advances to the next option in place, which makes it stateless in the UI sense, no edit mode to own, no draw-order rule to respect, at the cost of being slow to reach a distant option. The two controls here deliberately share ONE cell, so clicking either moves both: the cell is the state, and each control is only a view of it. |
| [<img src="../demos/list-view.png" width="180">](../demos/list-view.png) | `list-view` | `GuiListView` over a semicolon-separated string. Two cells, because raygui writes to both: one holds the scroll position (the index of the first visible row) and one the selection, where -1 means nothing is selected. Neither is remembered by the control between frames. The list holds more rows than fit, so the scrollbar is live. |
| [<img src="../demos/list-view-ex.png" width="180">](../demos/list-view-ex.png) | `list-view-ex` | `GuiListViewEx`, the list view that takes a real string array and reports focus. This is one of only two raygui functions taking a real `char**` array rather than a semicolon string, so it is where an array actually crosses the FFI boundary: jolt's `with-c-string-array` builds it and frees every member plus the array itself on the way out, so the control is called inside that body rather than the array being handed back. The extra cell is `:focus`, the row the pointer is over, which is not the same as the row that is selected. |
| [<img src="../demos/tab-bar.png" width="180">](../demos/tab-bar.png) | `tab-bar` | `GuiTabBar`, and the one result value that is not a yes or no. Every other control answers "nothing happened" or "something changed"; the tab bar has a third answer, `RESULT-TAB-CLOSE`, meaning the user clicked a tab's close box. raygui does not own the tab list, so it cannot remove anything: it reports which tab and the application decides. Here closing a tab really removes it, so the list shrinks. |

## containers

| preview | `bb` name | what it demonstrates |
|---|---|---|
| [<img src="../demos/panel-group-box.png" width="180">](../demos/panel-group-box.png) | `panel-group-box` | `GuiPanel` and `GuiGroupBox`, and what a container is not. Neither one contains anything: they draw a frame, they do not clip, do not own children, and do not offset what is drawn inside them. A control appears "inside" a panel only because its coordinates fall within the panel's rectangle and it was drawn afterwards, which is all containment means in immediate mode. The proof is the last group box, where a button is drawn deliberately overflowing it and nothing stops it. |
| [<img src="../demos/scroll-panel.png" width="180">](../demos/scroll-panel.png) | `scroll-panel` | `GuiScrollPanel`, the only container that really clips, and the only control taking two by-value Rectangles in one call: its own bounds and the size of the content behind it. raygui writes back the scroll offset and the visible region so the caller knows how to draw the content shifted; drawing the content into a scissor region offset by that scroll is the caller's job, since raygui clips its own chrome, not the caller's drawing. Port of raygui's own `scroll_panel` example. |
| [<img src="../demos/window-box.png" width="180">](../demos/window-box.png) | `window-box` | `GuiWindowBox`, a panel with a title bar and a close button. Closing it does nothing on raygui's side: there is no window object to destroy and no visibility flag to clear, the control just reports that the close button was clicked and the application stops calling it. Reopening is just calling it again. |
| [<img src="../demos/floating-window.png" width="180">](../demos/floating-window.png) | `floating-window` | Draggable windows, hand-rolled because raygui has no window manager. The application watches for a press inside a title bar, remembers the grab offset and moves its own coordinates; the window box itself never knows it moved. Two windows also make the draw-order rule concrete, since there is no z-order either: the one drawn last is the one on top, and clicking a window that is behind does not raise it. Port of raygui's own `floating_window` example. |

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
