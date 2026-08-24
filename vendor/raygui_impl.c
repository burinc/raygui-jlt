/*
 * The single compilation unit for raygui.
 *
 * raygui is a header-only library: declarations and implementation live in the
 * same file, and RAYGUI_IMPLEMENTATION must be defined in exactly ONE
 * translation unit. That makes this file the whole of the C in this project.
 *
 * Vendored revision: raygui 5.0-9-gfbf5d95 (see NOTICE).
 *
 * Built by `bb lib:build` into lib/libraygui.dylib (or .so on Linux), linked
 * DYNAMICALLY against libraylib. The dynamic link is load-bearing: raygui's
 * controls call GetMousePosition, DrawRectangle and friends internally, and
 * those must resolve to the same libraylib instance the jolt process has
 * already loaded. Linked statically, raygui would read input state from a
 * second, empty copy of raylib's globals and every control would be inert,
 * with no error to show for it.
 */
#define RAYGUI_IMPLEMENTATION
#include "raygui.h"
