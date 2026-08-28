## What this changes

<!-- One or two sentences. If it adds an example, name it and say whether it
     is a port of one of raygui's own examples or an original. -->

## Verification

CI runs on macOS and Ubuntu, builds libraygui from the vendored header,
asserts it links raylib dynamically, then compiles every example headlessly
and runs the registry, gallery, lint, format and clean-ns gates. What it
cannot do is look at a picture.

- [ ] `bb check:registration` passes
- [ ] `bb check` passes
- [ ] `bb lint:strict`, `bb lsp:format-check` and `bb lsp:clean-ns-check` pass

## If this adds or changes an example

<!-- Skip this section otherwise. -->

All five touchpoints. `bb check:registration` checks the first three and CI
runs it, so this list is here to save you a red build rather than to catch you
out.

- [ ] `src/net/b12n/raygui_jlt/<name_underscored>.clj`
- [ ] `deps.edn` alias, pointing at this example's own namespace
- [ ] `check.clj` require, added alphabetically
- [ ] a registry row in `scripts/examples_registry.clj`, description within
      49 characters
- [ ] a `bb.edn` task whose `:doc` matches the registry description
- [ ] a screenshot committed to `docs/demos/`, then `bb readme:examples`

### The screenshot

**This is the real gate.** Every other check proves the example compiles, is
linted and is formatted. None of them proves it renders correctly, and this
repo has shipped examples that passed all three while drawing the wrong thing.

```sh
RAYGUI_APP_AUTO_QUIT_MS=1500 RAYGUI_APP_SHOT=proof.png jolt -M:<alias>
```

Keep that path relative. raylib prepends the working directory, so an absolute
path silently writes nothing.

- [ ] I ran it and **looked at the PNG**
- [ ] The example shows enough state that the picture would look wrong if the
      control misbehaved

<!-- Say what the screenshot proves. "Scroll panel seeded mid-scroll, so the
     clipping shows", not "it looks right". -->

## If you touched the bindings or the build

<!-- Skip unless you changed raygui.clj, raylib.clj, vendor/ or bb.edn's
     build-argv. -->

- [ ] `bb lib:build` from clean, and libraygui still links raylib dynamically
      (`otool -L` on macOS, `ldd` on Linux). Statically linked raygui gets its
      own copy of raylib's globals and every control goes silently inert.
- [ ] Linux link order preserved: `-lraylib` must come after the translation
      unit, or the `.so` links cleanly with raylib undefined.

## Environment you tested on

- OS and arch (`uname -sm`):
- jolt (`jolt --version`):

## Notes for the reviewer

<!-- Anything surprising, any deliberate deviation from raygui's own example,
     anything you are unsure about. -->
