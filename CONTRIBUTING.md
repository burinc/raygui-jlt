# Contributing

Thanks for taking an interest. This is a suite of [raygui](https://github.com/raysan5/raygui)
examples written in [jolt](https://github.com/jolt-lang/jolt) (native Clojure on Chez
Scheme, no JVM), calling raygui directly over its C ABI through `jolt.ffi`.

New examples are welcome; the suite is deliberately mechanical to grow.

The documentation is published at <https://jlt-commons.github.io/raygui-jlt/>. It's generated from
this repo's `docs/guide/`; edit the Markdown here, never the site.

## Setting up

```sh
brew install raylib   # macOS; docs/guide/building-libraygui.md covers other platforms
bb lib:check           # is raylib installed, and is libraygui already built?
```

Every example task runs `bb lib:build` automatically if `lib/libraygui.dylib` is
missing, so a fresh clone needs no separate build step: `bb basic-controls` alone
is enough.

## Adding an example: the five touchpoints

One new example touches exactly five places. Miss one and the failure is often
silent, not a build error:

1. **Source**: `src/net/b12n/raygui_jlt/<name_underscored>.clj`.
2. **`deps.edn` alias**: `:<name> {:main-opts ["-m" "net.b12n.raygui-jlt.<name>"]}`.
3. **`check.clj` require**: add it alphabetically. **This one is the trap.** A
   missing `check.clj` require does not fail loudly: `bb check` simply never
   compiles your new namespace, so a broken example can sit in the repo passing
   every gate that runs. `bb check:registration` exists for exactly this, and
   CI runs it, but why it had to be a separate task is worth knowing: `bb check`
   cannot catch this on its own, because what it would have to notice is the
   thing it never loaded.
4. **Registry row**: `["<name>" "<alias>" "<group>" "<desc>"]` in
   `scripts/examples_registry.clj`, the single source of truth `bb info`,
   `bb examples` and the example catalog page are all generated from.
5. **`bb.edn` task**: `<name> {:doc "▶ <desc>" :task (run-example "<name>")}`.

Descriptions are capped at **49 characters** on every listing surface, enforced by
`bb examples`: it exits non-zero if a description is too long, and it also
cross-checks the registry's description against `bb.edn`'s `:doc` string for the
same example, exiting non-zero on any drift between the two. Run `bb examples`
after touching either one.

## Before you open a PR

```sh
bb check:registration  # all five touchpoints present and agreeing
bb check              # headless compile-check of every example (no window opens)
bb lint                # clj-kondo over src
bb lsp:format-check    # clojure-lsp formatting, dry run
```

`bb check:registration` needs no jolt, no raylib and no libraygui, so it runs
on a bare checkout in well under a second. It also catches a `deps.edn` alias
copy-pasted from its neighbour and left pointing at the wrong namespace, which
runs the wrong example rather than failing.

`bb hooks:install` sets up a local pre-commit hook that runs registration,
lint, format and clean-ns checks (~2s). It's never committed, so each clone
opts in. If you installed the hook before the registration check existed,
re-run the task: the file is written once rather than updated.

**None of the above is the real gate.** They prove your example compiles, is
linted and is formatted; they prove nothing about whether it renders correctly.
Every documented trap in this project's history passed all three.

## The screenshot requirement

**A new example needs a screenshot that was actually looked at.** Not generated
and filed away, looked at:

```sh
RAYGUI_APP_AUTO_QUIT_MS=1500 RAYGUI_APP_SHOT=proof.png jolt -M:<alias>
```

raylib **prepends the working directory** to whatever path you hand it, so a
relative path such as `docs/demos/x.png` lands where you expect and an absolute
path becomes cwd + path and silently writes nothing. Keep the path relative. The
helper checks the file appeared and prints `SHOT FAILED` if it did not.

This project has shipped multiple examples that compiled cleanly, passed lint
and format, and rendered a plausible, wrong result: a scroll panel filling the
window instead of clipping to its declared size, a style color with red and blue
swapped, a dialog whose Cancel and OK buttons silently did each other's job.
None of those threw an exception. All of them were visible the moment someone
looked at the PNG. See
[`docs/guide/what-the-gates-do-not-catch.md`](docs/guide/what-the-gates-do-not-catch.md)
for the full list and how each was found.

For a control whose result encodes more than "pressed / not pressed" (a button
index, a close request, a style flag), read the vendored C source
(`vendor/raygui.h`) for what the return value actually means before writing the
example. A screenshot shows a dialog open; it never shows what its buttons do.

## Definitions must precede use

Since jolt 0.4.0, an unresolved symbol is a **compile error**, not a late-bound
reference. `src/net/b12n/raygui_jlt/raygui.clj` is the shared bindings layer every
example depends on, and its section order is load-bearing: a new binding or helper
must be **appended**, never interleaved earlier in the file, or you risk moving a
definition after something that already uses it. `bb check` is the quick way to
confirm the whole suite still loads after an edit here.

## Rendered strings

Two rules for any string that raygui actually draws (`:text`, `:title`,
`:message`, `:left`, `:right`, or anything passed to `rl/text!`):

- **ASCII only.** raygui's default font has no glyph for an em-dash; it draws as a
  literal `?`. This applies only to strings that are rendered on screen.
  Docstrings and comments are unaffected, since they are never drawn.
- **Must fit the control's `:w`.** raygui does not wrap text; it cuts off
  mid-word once it runs out of room. Size your `:w` to the string, or shorten the
  string to the box.

If you're checking a string's rendered width from code rather than by eye, know
that `GuiGetTextWidth` returns `0` for every string when no window is open, so a
headless width check silently reports everything as fitting. Measure with a window
open, or trust the screenshot instead.

## Staging your commit

Stage files by explicit path; never `git add -A`, `git add .`, or `git add -u`. If
you see an untracked file you didn't create, mention it in your PR rather than
sweeping it in.

## Licensing

This project is released under the zlib/libpng license, the same license as raygui
and raylib themselves. By contributing, you agree your contribution is licensed
under those terms. If your example is a port of one of raygui's own example
programs, please say so in your PR so the attribution in `NOTICE` stays accurate.
