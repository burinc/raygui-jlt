# Building `libraygui`

The page with no equivalent in the sibling repo, and the one a reader most needs.

`b12n-raylib-jlt` loads a system-installed `libraylib`: `brew install raylib` and
you're done. raygui has no such option. **raygui is header-only**: upstream ships a
single `raygui.h` you `#include` with `RAYGUI_IMPLEMENTATION` defined once, and there
is no `libraygui.so`, no Homebrew formula, and nothing on any distro's package index
to install. `jolt.ffi` needs a shared library to load, so this repo builds its own.

## What `vendor/` holds

- `vendor/raygui.h`: a pinned, unmodified copy of upstream's header at revision
  `5.0-9-gfbf5d95`.
- `vendor/raygui_impl.c`: the one compilation unit. Two lines:
  `#define RAYGUI_IMPLEMENTATION` then `#include "raygui.h"`.

## What `bb lib:build` does

It runs a compiler invocation defined in `bb.edn`. On macOS:

```sh
cc -O2 -dynamiclib -fPIC -DBUILD_LIBTYPE_SHARED \
   -I vendor \
   -I /opt/homebrew/include \
   -L /opt/homebrew/lib -lraylib \
   -framework CoreVideo -framework IOKit -framework Cocoa -framework OpenGL \
   -o lib/libraygui.dylib vendor/raygui_impl.c
```

The Homebrew include/lib paths are named explicitly because they aren't on the
compiler's default search path. `bb lib:check` reports whether raylib is installed
and whether `libraygui` is already built, with the fix for each; every example task
runs `bb lib:build` automatically if the library is missing, so a fresh clone needs
no separate setup step.

## Why the raylib link must be dynamic

This is the load-bearing part of the whole build, and it fails silently if you get
it wrong.

raygui's controls call raylib's own functions internally: `GetMousePosition`,
`DrawRectangle`, `MeasureTextEx`, and more, every frame. Those calls must reach
**the same libraylib instance** that the jolt process has already loaded, or raygui
reads mouse position and draws geometry against a second, independent copy of
raylib's global state, one that never receives input events and never gets flushed
to the real framebuffer. The result: every control renders (raygui draws its own
chrome) but nothing ever responds to input, because the click landed in the jolt
process's libraylib instance while the control checked a different one.

**A statically-linked raygui would compile and load without error, and every control
would simply be inert.** There is no crash, no warning, nothing in the log to point
at. Dynamic linkage gives raygui and jolt the same libraylib instance for free,
because both resolve the same shared library at load time; `-lraylib` in the build
invocation above links dynamically by default, so getting this right on macOS is a
matter of *not* accidentally adding a static-link flag, not adding one.

## Verifying a build

Two checks, both read-only:

```sh
nm -gU lib/libraygui.dylib | grep -c ' T _Gui'
# 61

otool -L lib/libraygui.dylib
# lib/libraygui.dylib:
#         lib/libraygui.dylib (compatibility version 0.0.0, current version 0.0.0)
#         /opt/homebrew/opt/raylib/lib/libraylib.600.dylib (compatibility version 600.0.0, current version 6.0.0)
#         /System/Library/Frameworks/CoreVideo.framework/...
#         /System/Library/Frameworks/IOKit.framework/...
#         /System/Library/Frameworks/Cocoa.framework/...
#         /System/Library/Frameworks/OpenGL.framework/...
#         /usr/lib/libSystem.B.dylib
```

`nm -gU` reports **61 exported `T _Gui*` symbols**, matching the 61 `RAYGUIAPI`
declarations in `vendor/raygui.h`. `otool -L` is the dynamic-link check itself: the
second line naming `libraylib.600.dylib` (not a static blob folded into the binary)
is what makes the previous section true. If that line is missing, or points
somewhere other than the raylib you expect, the build has silently gone wrong in
exactly the way that produces inert controls.

## Linux: untested

```sh
cc -O2 -shared -fPIC -DBUILD_LIBTYPE_SHARED \
   -I vendor \
   -lraylib -lGL -lm -lpthread -ldl -lrt -lX11 \
   -o lib/libraygui.so vendor/raygui_impl.c
```

These flags are written from the macOS invocation, using the distro-default include
and library search paths instead of Homebrew's. **They have not been run.** Do not
treat them as working until someone builds and screenshots at least one example on
Linux.

## Bumping the vendored header

1. Replace `vendor/raygui.h` with the new revision.
2. Update the pinned revision string in `NOTICE`.
3. `bb lib:build` to recompile against it.
4. `bb check` to confirm every example still compiles against any signature changes.
5. Re-run and re-screenshot at least `scroll-panel`, the example most exposed to a
   layout or style regression (it is the one control taking two by-value
   `Rectangle`s in a single call), and any example whose control depends on the
   currently-loaded style once the styling group lands.

## See also

- [`the-ffi-shape.md`](the-ffi-shape.md): what this dynamically-linked library lets
  jolt call, and how.
- [`what-the-gates-do-not-catch.md`](what-the-gates-do-not-catch.md): the byte-swapped
  style-color trap, one layer up from the build itself.
