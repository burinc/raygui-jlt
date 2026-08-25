(ns net.b12n.raygui-jlt.raylib
  "The slice of raylib these raygui examples need: window lifecycle, the frame
  loop, input, colours, text and the two rectangle primitives, plus the headless
  smoke-test helpers.

  This is a trimmed derivative of the bindings in the sibling project
  b12n-raylib-jlt, which carries the full 109-symbol surface. raygui does its own
  drawing, so the examples here need raylib only to open a window, run a frame
  and read input.

  raylib is the system libraylib, declared as a :jolt/native lib in deps.edn and
  called directly over its C ABI. Every drawing call here uses raylib's
  scalar-argument variants, so the only by-value struct crossing this boundary is
  Color, a 4-byte {u8 r,g,b,a} packed into a :uint (see `rgba`)."
  (:require
   [jolt.ffi :as ffi]))

;; --- Color -------------------------------------------------------------------
;; Defined first: the drawing wrappers below reference the palette in their :or
;; defaults, and since jolt 0.4.0 a symbol must be defined before its first use
;; in the file — in an :or default as much as at top level. Keep this section
;; above its first use; append below, never interleave.
(defn rgba
  "Pack an RGBA colour into the little-endian uint32 that raylib's `Color` struct
  is (r | g<<8 | b<<16 | a<<24), so it can cross the FFI boundary as a :uint."
  [r g b a]
  (bit-or (int r) (bit-shift-left (int g) 8)
          (bit-shift-left (int b) 16) (bit-shift-left (int a) 24)))

;; raylib's named colour palette (values from src/raylib.h).
(def LIGHTGRAY (rgba 200 200 200 255))   (def GRAY       (rgba 130 130 130 255))
(def DARKGRAY  (rgba 80 80 80 255))      (def YELLOW     (rgba 253 249 0 255))
(def GOLD      (rgba 255 203 0 255))     (def ORANGE     (rgba 255 161 0 255))
(def PINK      (rgba 255 109 194 255))   (def RED        (rgba 230 41 55 255))
(def MAROON    (rgba 190 33 55 255))     (def GREEN      (rgba 0 228 48 255))
(def LIME      (rgba 0 158 47 255))      (def DARKGREEN  (rgba 0 117 44 255))
(def SKYBLUE   (rgba 102 191 255 255))   (def BLUE       (rgba 0 121 241 255))
(def DARKBLUE  (rgba 0 82 172 255))      (def PURPLE     (rgba 200 122 255 255))
(def VIOLET    (rgba 135 60 190 255))    (def DARKPURPLE (rgba 112 31 126 255))
(def BEIGE     (rgba 211 176 131 255))   (def BROWN      (rgba 127 106 79 255))
(def DARKBROWN (rgba 76 63 47 255))      (def WHITE      (rgba 255 255 255 255))
(def BLACK     (rgba 0 0 0 255))         (def MAGENTA    (rgba 255 0 255 255))
(def RAYWHITE  (rgba 245 245 245 255))

;; GetColor converts raygui's 0xRRGGBBAA style values into raylib's packed
;; 0xAABBGGRR Color. This is not a convenience: feeding a GuiGetStyle value
;; straight to a raylib colour argument swaps red and blue and renders a
;; plausible wrong colour with no error. See rg/style-color, which wraps this,
;; and AGENTS.md "Two traps".
(ffi/defcfn get-color "GetColor" [:uint] :uint)

;; --- window / lifecycle ------------------------------------------------------
(ffi/defcfn init-window    "InitWindow"   [:int :int :string] :void)
(ffi/defcfn set-target-fps "SetTargetFPS" [:int] :void)
(ffi/defcfn close-window   "CloseWindow"  [] :void)
(ffi/defcfn get-screen-width  "GetScreenWidth"  [] :int)
(ffi/defcfn get-screen-height "GetScreenHeight" [] :int)
(ffi/defcfn ^:private should-close-raw "WindowShouldClose" [] :int)

;; --- frame -------------------------------------------------------------------
(ffi/defcfn begin-drawing    "BeginDrawing"    [] :void)
(ffi/defcfn end-drawing      "EndDrawing"      [] :void)
(ffi/defcfn clear-background "ClearBackground" [:uint] :void)   ; Color
(ffi/defcfn get-frame-time   "GetFrameTime"    [] :float)
(ffi/defcfn get-time         "GetTime"         [] :double)

;; --- 2D shapes + text (scalar variants; Color is the only by-value struct) ---
(ffi/defcfn draw-text            "DrawText"           [:string :int :int :int :uint] :void)
(ffi/defcfn draw-fps             "DrawFPS"            [:int :int] :void)
(ffi/defcfn measure-text         "MeasureText"        [:string :int] :int)
(ffi/defcfn draw-rectangle       "DrawRectangle"      [:int :int :int :int :uint] :void)
(ffi/defcfn draw-rectangle-lines "DrawRectangleLines" [:int :int :int :int :uint] :void)
(ffi/defcfn draw-line            "DrawLine"           [:int :int :int :int :uint] :void)
(ffi/defcfn draw-circle          "DrawCircle"         [:int :int :float :uint] :void)
(ffi/defcfn begin-scissor-mode   "BeginScissorMode"   [:int :int :int :int] :void)
(ffi/defcfn end-scissor-mode     "EndScissorMode"     [] :void)

;; --- input -------------------------------------------------------------------
(ffi/defcfn ^:private key-down-raw      "IsKeyDown"            [:int] :int)
(ffi/defcfn ^:private key-pressed-raw   "IsKeyPressed"         [:int] :int)
(ffi/defcfn ^:private mouse-down-raw    "IsMouseButtonDown"    [:int] :int)
(ffi/defcfn ^:private mouse-pressed-raw "IsMouseButtonPressed" [:int] :int)
(ffi/defcfn get-mouse-x      "GetMouseX"         [] :int)
(ffi/defcfn get-mouse-y      "GetMouseY"         [] :int)
(ffi/defcfn get-mouse-wheel  "GetMouseWheelMove" [] :float)
(ffi/defcfn get-random-value "GetRandomValue"    [:int :int] :int)

;; C-bool returns arrive in the low byte; mask so only 0/1 counts.
(defn window-should-close?
  []
  (not (zero? (bit-and (should-close-raw) 0xff))))

(defn key-down?
  [k]
  (not (zero? (bit-and (key-down-raw k) 0xff))))

(defn key-pressed?
  [k]
  (not (zero? (bit-and (key-pressed-raw k) 0xff))))

(defn mouse-down?
  [b]
  (not (zero? (bit-and (mouse-down-raw b) 0xff))))

(defn mouse-pressed?
  [b]
  (not (zero? (bit-and (mouse-pressed-raw b) 0xff))))

;; --- constants (raylib KeyboardKey / MouseButton) ----------------------------
(def ^:const KEY-SPACE 32)  (def ^:const KEY-R     82)
(def ^:const KEY-W     87)  (def ^:const KEY-A     65)
(def ^:const KEY-S     83)  (def ^:const KEY-D     68)
(def ^:const KEY-RIGHT 262) (def ^:const KEY-LEFT  263)
(def ^:const KEY-DOWN  264) (def ^:const KEY-UP    265)
(def ^:const KEY-ENTER 257) (def ^:const KEY-BACKSPACE 259)
(def ^:const KEY-TAB   258) (def ^:const KEY-ESCAPE 256)
(def ^:const MOUSE-LEFT 0)  (def ^:const MOUSE-RIGHT 1)

;; --- ergonomic keyword-argument drawing API ----------------------------------
;; raylib's C functions are positional; these wrappers take keyword arguments so
;; example code reads self-descriptively. The raw bindings above remain the FFI
;; boundary; these just name the arguments.

(defn window!
  "InitWindow with keyword args. :width :height :title."
  [& {:keys [width height title]
      :or {width 800
           height 450
           title "raygui"}}]
  (init-window width height title))

(defn text!
  "DrawText. :x :y :size :color."
  [s & {:keys [x y size color]
        :or {x 0
             y 0
             size 20
             color DARKGRAY}}]
  (draw-text s x y size color))

(defn rect!
  "DrawRectangle. :x :y :width :height :color."
  [& {:keys [x y width height color]
      :or {x 0
           y 0
           width 10
           height 10
           color LIGHTGRAY}}]
  (draw-rectangle x y width height color))

(defn rect-lines!
  "DrawRectangleLines. :x :y :width :height :color."
  [& {:keys [x y width height color]
      :or {x 0
           y 0
           width 10
           height 10
           color DARKGRAY}}]
  (draw-rectangle-lines x y width height color))

(defn line!
  "DrawLine. :x1 :y1 :x2 :y2 :color."
  [& {:keys [x1 y1 x2 y2 color]
      :or {x1 0
           y1 0
           x2 0
           y2 0
           color DARKGRAY}}]
  (draw-line x1 y1 x2 y2 color))

(defn circle!
  "DrawCircle. :x :y :radius :color."
  [& {:keys [x y radius color]
      :or {x 0
           y 0
           radius 10
           color DARKGRAY}}]
  (draw-circle x y (double radius) color))

;; --- smoke-test loop guards --------------------------------------------------
(defn auto-quit-deadline
  "RAYGUI_APP_AUTO_QUIT_MS=<n> ends the loop after n ms, so a window example is
  smoke-testable with no person at the keyboard. Returns an absolute ms deadline
  or nil."
  []
  (when-let [v (System/getenv "RAYGUI_APP_AUTO_QUIT_MS")]
    (try (let [ms (Integer/parseInt v)]
           (when (pos? ms) (+ (System/currentTimeMillis) ms)))
         (catch Exception _ nil))))

(defn keep-running?
  "True while the window is open and any RAYGUI_APP_AUTO_QUIT_MS deadline is unmet."
  [deadline]
  (and (not (window-should-close?))
       (or (nil? deadline) (< (System/currentTimeMillis) deadline))))

(ffi/defcfn take-screenshot       "TakeScreenshot"          [:string] :void)
(ffi/defcfn ^:private flush-batch "rlDrawRenderBatchActive" [] :void)

(def ^:private shot-path (System/getenv "RAYGUI_APP_SHOT"))

(defn maybe-screenshot!
  "RAYGUI_APP_SHOT=/path dumps one PNG on frame `at` — headless visual proof a
  frame rendered.

  Flushes raylib's batched geometry FIRST. This is not optional: raylib defers
  DrawText and friends until EndDrawing, so a mid-frame TakeScreenshot without
  the flush writes a perfectly blank frame. A blank PNG means this flush is
  missing, not that the example drew nothing.

  raylib writes the file's basename into the current working directory."
  [frame at]
  (when (and shot-path (= frame at))
    (flush-batch)
    (take-screenshot shot-path)
    (binding [*out* *err*] (println "[net.b12n.raygui-jlt] SHOT" shot-path))))
