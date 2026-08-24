(ns net.b12n.raygui-jlt.raygui
  "jolt.ffi bindings for raygui, plus a keyword-argument control API.

  raygui is an immediate-mode GUI library for raylib. It keeps NO per-control
  state: the application owns every value and passes a pointer to it, and the
  control returns an int saying what happened. That is what makes it easy to bind
  and hard to leak.

  raygui is header-only and ships no shared library, so lib/libraygui.dylib is
  built from the vendored header by `bb lib:build` and declared as a
  :jolt/native lib in deps.edn. See AGENTS.md.

  Two shapes recur through every control:

    * `Rectangle bounds` BY VALUE — 16 bytes of homogeneous float. Passed
      through ONE module-level scratch buffer (see the scratch section), because
      jolt copies the struct at call time rather than retaining the pointer. No
      caller allocates a Rectangle.
    * a pointer out-param carrying application state — a `cell` here.

  59 of raygui's 61 functions are bound. GuiSetFont/GuiGetFont pass raylib's
  Font struct by value and nothing needs them: the vendored styles carry and
  apply their own embedded fonts.

  Section order is load-bearing (jolt 0.4.0: unresolved symbols are compile
  errors, including in :or defaults). Append; never interleave."
  (:require
   [jolt.ffi :as ffi]
   [net.b12n.raygui-jlt.raylib :as rl]))

;; --- raw bindings: global state ----------------------------------------------
(ffi/defcfn gui-enable          "GuiEnable"          [] :void)
(ffi/defcfn gui-disable         "GuiDisable"         [] :void)
(ffi/defcfn gui-lock            "GuiLock"            [] :void)
(ffi/defcfn gui-unlock          "GuiUnlock"          [] :void)
(ffi/defcfn ^:private gui-is-locked-raw "GuiIsLocked" [] :int)
(ffi/defcfn gui-set-alpha       "GuiSetAlpha"        [:float] :void)
(ffi/defcfn gui-set-state       "GuiSetState"        [:int] :void)
(ffi/defcfn gui-get-state       "GuiGetState"        [] :int)
(ffi/defcfn gui-enable-tooltip  "GuiEnableTooltip"   [] :void)
(ffi/defcfn gui-disable-tooltip "GuiDisableTooltip"  [] :void)
(ffi/defcfn gui-set-tooltip     "GuiSetTooltip"      [:string] :void)

;; --- raw bindings: style -----------------------------------------------------
(ffi/defcfn gui-set-style              "GuiSetStyle"             [:int :int :int] :void)
(ffi/defcfn gui-get-style              "GuiGetStyle"             [:int :int] :int)
(ffi/defcfn gui-load-style             "GuiLoadStyle"            [:string] :void)
(ffi/defcfn gui-load-style-from-memory "GuiLoadStyleFromMemory"  [:pointer :int] :void)
(ffi/defcfn gui-load-style-default     "GuiLoadStyleDefault"     [] :void)

;; --- raw bindings: icons -----------------------------------------------------
(ffi/defcfn gui-icon-text      "GuiIconText"     [:int :string] :string)
(ffi/defcfn gui-set-icon-scale "GuiSetIconScale" [:int] :void)
(ffi/defcfn gui-get-icons      "GuiGetIcons"     [] :pointer)
(ffi/defcfn gui-load-icons     "GuiLoadIcons"    [:string :int] :pointer)
(ffi/defcfn gui-load-icons-from-memory "GuiLoadIconsFromMemory" [:pointer :int :int] :pointer)
(ffi/defcfn gui-draw-icon      "GuiDrawIcon"     [:int :int :int :int :uint] :void)  ; Color
(ffi/defcfn gui-get-text-width "GuiGetTextWidth" [:string] :int)

;; --- raw bindings: controls --------------------------------------------------
;; Every `Rectangle bounds` is [:by-value [:struct ...]] spelled out in full.
(ffi/defcfn gui-window-box "GuiWindowBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-group-box "GuiGroupBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-line "GuiLine"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-panel "GuiPanel"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-scroll-panel "GuiScrollPanel"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string
   [:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :pointer :pointer] :int)
(ffi/defcfn gui-label "GuiLabel"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-button "GuiButton"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-label-button "GuiLabelButton"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-toggle "GuiToggle"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-toggle-group "GuiToggleGroup"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-toggle-slider "GuiToggleSlider"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-check-box "GuiCheckBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-combo-box "GuiComboBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-dropdown-box "GuiDropdownBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :int] :int)
(ffi/defcfn gui-spinner "GuiSpinner"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :int :int :int] :int)
(ffi/defcfn gui-value-box "GuiValueBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :int :int :int] :int)
(ffi/defcfn gui-value-box-float "GuiValueBoxFloat"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :pointer :int] :int)
(ffi/defcfn gui-text-box "GuiTextBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :pointer :int :int] :int)
(ffi/defcfn gui-slider "GuiSlider"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :string :pointer :float :float] :int)
(ffi/defcfn gui-slider-bar "GuiSliderBar"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :string :pointer :float :float] :int)
(ffi/defcfn gui-progress-bar "GuiProgressBar"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :string :pointer :float :float] :int)
(ffi/defcfn gui-status-bar "GuiStatusBar"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-dummy-rec "GuiDummyRec"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string] :int)
(ffi/defcfn gui-grid "GuiGrid"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :float :int :pointer] :int)
(ffi/defcfn gui-list-view "GuiListView"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :pointer] :int)
(ffi/defcfn gui-list-view-ex "GuiListViewEx"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :pointer :int :pointer :pointer :pointer] :int)
(ffi/defcfn gui-tab-bar "GuiTabBar"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer :pointer] :int)
(ffi/defcfn gui-tab-bar-ex "GuiTabBarEx"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :pointer :int :pointer :pointer :pointer] :int)
(ffi/defcfn gui-message-box "GuiMessageBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :string :string :pointer] :int)
(ffi/defcfn gui-text-input-box "GuiTextInputBox"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :string :pointer :int :string :pointer :pointer] :int)
(ffi/defcfn gui-color-picker "GuiColorPicker"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-color-panel "GuiColorPanel"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-color-bar-alpha "GuiColorBarAlpha"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-color-bar-hue "GuiColorBarHue"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-color-picker-hsv "GuiColorPickerHSV"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)
(ffi/defcfn gui-color-panel-hsv "GuiColorPanelHSV"
  [[:by-value [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]] :string :pointer] :int)

(defn gui-locked?
  "C-bool returns arrive in the low byte; mask so only 0/1 counts."
  []
  (not (zero? (bit-and (gui-is-locked-raw) 0xff))))

;; --- constants ---------------------------------------------------------------
;; GuiResult — every control returns one of these (raygui 5.0's API-breaking
;; change made all controls return int).
(def ^:const RESULT-NONE      0)
(def ^:const RESULT-PRESSED   1)
(def ^:const RESULT-CHANGED   2)
(def ^:const RESULT-TAB-CLOSE 4)

;; GuiState — GuiSetState / GuiGetState.
(def ^:const STATE-NORMAL   0)
(def ^:const STATE-FOCUSED  1)
(def ^:const STATE-PRESSED  2)
(def ^:const STATE-DISABLED 3)

;; GuiControl — the first argument to GuiSetStyle / GuiGetStyle.
(def ^:const DEFAULT     0)   (def ^:const LABEL       1)
(def ^:const BUTTON      2)   (def ^:const TOGGLE      3)
(def ^:const SLIDER      4)   (def ^:const PROGRESSBAR 5)
(def ^:const CHECKBOX    6)   (def ^:const COMBOBOX    7)
(def ^:const DROPDOWNBOX 8)   (def ^:const TEXTBOX     9)
(def ^:const VALUEBOX    10)  (def ^:const TABBAR      11)
(def ^:const LISTVIEW    12)  (def ^:const COLORPICKER 13)
(def ^:const SCROLLBAR   14)  (def ^:const STATUSBAR   15)

;; GuiControlProperty — base properties every control has.
(def ^:const BORDER-COLOR-NORMAL   0)  (def ^:const BASE-COLOR-NORMAL   1)
(def ^:const TEXT-COLOR-NORMAL     2)  (def ^:const BORDER-COLOR-FOCUSED 3)
(def ^:const BASE-COLOR-FOCUSED    4)  (def ^:const TEXT-COLOR-FOCUSED  5)
(def ^:const BORDER-COLOR-PRESSED  6)  (def ^:const BASE-COLOR-PRESSED  7)
(def ^:const TEXT-COLOR-PRESSED    8)  (def ^:const BORDER-COLOR-DISABLED 9)
(def ^:const BASE-COLOR-DISABLED   10) (def ^:const TEXT-COLOR-DISABLED 11)
(def ^:const BORDER-WIDTH          12) (def ^:const TEXT-PADDING        13)
(def ^:const TEXT-ALIGNMENT        14)

;; GuiDefaultProperty — extra properties on the DEFAULT control only.
(def ^:const TEXT-SIZE        16)
(def ^:const TEXT-SPACING     17)
(def ^:const LINE-COLOR       18)
(def ^:const BACKGROUND-COLOR 19)
(def ^:const TEXT-LINE-SPACING 20)

;; GuiTextAlignment
(def ^:const TEXT-ALIGN-LEFT   0)
(def ^:const TEXT-ALIGN-CENTER 1)
(def ^:const TEXT-ALIGN-RIGHT  2)

;; --- the scratch Rectangle ---------------------------------------------------
;; Every control takes `Rectangle bounds` by value. jolt's [:by-value ...] wants
;; a pointer to caller-owned storage and COPIES the struct at call time, so one
;; module-level buffer rewritten per call is sufficient and correct.
;;
;; Verified rather than assumed: four controls drawn at four different positions
;; through this single buffer render identically to four separate allocations.
;;
;; The consequence is the point. Without it every control call would allocate 16
;; bytes per frame — 4 controls at 60fps is 240 allocations a second, per
;; example, all needing a matching free. With it, NO example allocates or frees
;; inside a frame, and the only lifetime an example manages is its cells.
;;
;; Not thread-safe, and does not need to be: raylib's frame loop is single
;; threaded and every control call returns before the next begins.
(def ^:private rect-layout
  (ffi/layout [:struct [[:x :float] [:y :float] [:width :float] [:height :float]]]))

(def ^:private scratch-rect (ffi/alloc (ffi/layout-size rect-layout)))

;; A SECOND buffer, for the one control that takes two by-value Rectangles in a
;; single call: GuiScrollPanel(bounds, text, content, *scroll, *view).
;;
;; Clojure evaluates arguments left to right, so writing both rects through one
;; buffer means the second write clobbers the first and BOTH pointers hand raygui
;; the same rectangle. Measured: a scroll panel declared at 200x90 rendered at its
;; 600x400 content size, covering the window, with no error. The panel still
;; drew, scrollbars and all, which is what makes it dangerous.
;;
;; Two buffers is the whole fix. There is no third case: no other raygui function
;; takes more than one by-value struct.
(def ^:private scratch-content (ffi/alloc (ffi/layout-size rect-layout)))

(defn- write-rect!
  [p x y w h]
  (ffi/write-field p rect-layout :x (double x))
  (ffi/write-field p rect-layout :y (double y))
  (ffi/write-field p rect-layout :width (double w))
  (ffi/write-field p rect-layout :height (double h))
  p)

(defn bounds!
  "Rewrite the scratch Rectangle and return it. Internal: the kwarg wrappers
  below call this, and examples pass :x :y :w :h instead."
  [x y w h]
  (write-rect! scratch-rect x y w h))

(defn content!
  "Rewrite the SECOND scratch Rectangle and return it.

  Used only by scroll-panel!, for the content rect that accompanies bounds in
  the same call. Never use this where bounds! would do: the point of two buffers
  is that one call can hold both alive, not that there are two interchangeable
  scratch slots."
  [x y w h]
  (write-rect! scratch-content x y w h))

;; --- cells -------------------------------------------------------------------
;; raygui keeps no state: the application owns every value and hands the control
;; a pointer to it. A cell is that pointer plus the type needed to read it back.
;;
;; Allocate cells ONCE, outside the frame loop, and free them after it. A cell
;; allocated inside the loop leaks 60 times a second.
;;
;; Sizes: :bool is a C bool (1 byte), :int and :float 4, :color a packed RGBA
;; uint (4), :vector2 two floats (8), :vector3 three floats (12).
(def ^:private cell-sizes
  {:float 4
   :int 4
   :bool 1
   :color 4
   :vector2 8
   :vector3 12
   :rect 16})

(defn- live-ptr
  "The cell's pointer, or throw if it has already been freed.

  This guard exists because the two failure modes are both terrible and one is
  undebuggable. Measured on this jolt build:

    * reading a freed cell returns GARBAGE, silently — a freed :int cell that
      held 5 read back as 7222, with no error;
    * freeing a cell twice ABORTS THE PROCESS: exit 133, no output, no stack,
      and NOT interceptable by try/catch, because it happens in the allocator
      below the Clojure layer.

  Each example frees its cells by hand at teardown, so across the suite there
  are dozens of hand-written free-cell! calls. Turning a silent process abort
  into an ordinary catchable exception is worth six lines."
  [c]
  (when @(:freed? c)
    (throw (ex-info "cell used after free-cell!" {:type (:type c)})))
  (:ptr c))

(defn ptr
  "The raw pointer inside a cell, for passing to a raw gui-* binding."
  [c]
  (live-ptr c))

(defn value
  "Read a cell, typed. :bool returns true/false; :text returns a Clojure string;
  :rect returns [x y w h]; :vector2 returns [x y]; :vector3 returns [x y z];
  :float, :int and :color return a number."
  [c]
  (let [p (live-ptr c)]
    (case (:type c)
      :float   (ffi/read p :float 0)
      :int     (ffi/read p :int 0)
      :color   (ffi/read p :uint 0)
      :bool    (not (zero? (bit-and (ffi/read p :uint8 0) 0xff)))
      :text    (ffi/ptr->string p)
      :vector2 [(ffi/read p :float 0) (ffi/read p :float 4)]
      :vector3 [(ffi/read p :float 0) (ffi/read p :float 4) (ffi/read p :float 8)]
      :rect    [(ffi/read p :float 0) (ffi/read p :float 4)
                (ffi/read p :float 8) (ffi/read p :float 12)])))

;; reset-cell! is defined BEFORE cell, which calls it. Since jolt 0.4.0 a symbol
;; must be defined before its first use in the file, in a fn body as much as at
;; top level, so the reverse order is a compile error rather than a style choice.
(defn reset-cell!
  "Write `v` into a cell. Returns v."
  [c v]
  (let [p (live-ptr c)]
    (case (:type c)
      :float   (ffi/write p :float 0 (double v))
      :int     (ffi/write p :int 0 (int v))
      :color   (ffi/write p :uint 0 v)
      :bool    (ffi/write p :uint8 0 (if v 1 0))
      :text    (let [size (:size c)
                     bs (.getBytes (str v) "UTF-8")
                     n (min (alength bs) (dec size))]
                 ;; Zero the whole buffer first: raygui edits it in place, so a
                 ;; shorter new value must not leave the old tail behind the NUL
                 ;; for the next edit to resurrect.
                 (dotimes [i size] (ffi/write p :uint8 i 0))
                 (dotimes [i n] (ffi/write p :uint8 i (bit-and (aget bs i) 0xff))))
      :vector2 (do (ffi/write p :float 0 (double (nth v 0)))
                   (ffi/write p :float 4 (double (nth v 1))))
      :vector3 (do (ffi/write p :float 0 (double (nth v 0)))
                   (ffi/write p :float 4 (double (nth v 1)))
                   (ffi/write p :float 8 (double (nth v 2))))
      :rect    (do (ffi/write p :float 0 (double (nth v 0)))
                   (ffi/write p :float 4 (double (nth v 1)))
                   (ffi/write p :float 8 (double (nth v 2)))
                   (ffi/write p :float 12 (double (nth v 3)))))
    v))

(defn free-cell!
  "Release a cell. Call once, after the frame loop.

  Idempotent on purpose: a second call is a no-op rather than a process abort.
  See live-ptr for the measurement."
  [c]
  (when (compare-and-set! (:freed? c) false true)
    (ffi/free (:ptr c)))
  nil)

(defn cell
  "Allocate a native cell of `type` holding `init`.

  type is one of :float :int :bool :color :vector2 :vector3 :rect. For :vector2
  the init is [x y]; for :vector3 [x y z]; for :rect [x y w h]; for :bool a
  truthy value; for :color a packed rgba uint (see rl/rgba).

  :text is deliberately NOT reachable here — a text buffer needs an explicit
  size, so it goes through text-cell."
  [type init]
  (let [size (get cell-sizes type)]
    (when (nil? size)
      (throw (ex-info "unknown cell type" {:type type
                                           :known (keys cell-sizes)})))
    (let [p (ffi/alloc size)
          c {:ptr p
             :type type
             :freed? (atom false)}]
      (reset-cell! c init)
      c)))

(defn text-cell
  "Allocate a mutable char buffer of `size` bytes holding `init`.

  Separate from `cell` because a text buffer is the one cell whose size is not
  implied by its type: GuiTextBox and GuiTextInputBox edit the buffer IN PLACE
  and need to know how much room they have. `size` includes the NUL terminator,
  so a 64-byte cell holds 63 characters.

  Read it with `value`, which returns a Clojure string.

  Truncation is by UTF-8 BYTE, not by codepoint, so a multi-byte character
  straddling the limit is cut mid-sequence and reads back with a replacement
  character (measured: a size-4 cell given \"ee\" with both e-acute reads back as
  one e-acute plus U+FFFD). Harmless for the ASCII these controls carry in
  practice — numbers, filenames, short labels — but do not seed a text-cell with
  non-ASCII content close to its size."
  [size init]
  (let [p (ffi/alloc size)
        c {:ptr p
           :type :text
           :size size
           :freed? (atom false)}]
    (reset-cell! c init)
    c))

;; --- style helpers -----------------------------------------------------------
(defn style-color
  "The style's colour for `control`/`property`, as a raylib packed Color.

  raygui stores style colours as 0xRRGGBBAA; raylib's Color packs little-endian
  as 0xAABBGGRR. Passing a GuiGetStyle value straight to a raylib colour
  argument swaps red and blue and renders a plausible WRONG colour, with no
  error to notice. Measured: cyber's background is 0x81C0D0FF, RGB(129,192,208),
  a light blue; fed raw to ClearBackground it renders salmon pink.

  Always come through here. See AGENTS.md \"Two traps\"."
  [control property]
  (rl/get-color (gui-get-style control property)))

(defn load-style!
  "Load a .rgs style file. Returns the byte count read.

  Reads the bytes Clojure-side and hands raygui a buffer rather than calling
  GuiLoadStyle(path), which resolves its path against the process working
  directory: an example that works under `bb` from the repo root would break run
  from anywhere else. The .rgs format embeds its own font, so nothing else is
  needed.

  Verified against style_cyber.rgs: 3884 bytes, palette and embedded font both
  applied on the next frame."
  [path]
  (let [bs (java.nio.file.Files/readAllBytes (.toPath (java.io.File. path)))
        n (alength bs)
        p (ffi/alloc n)]
    (try
      (dotimes [i n]
        (ffi/write p :uint8 i (bit-and (aget bs i) 0xff)))
      (gui-load-style-from-memory p n)
      n
      (finally (ffi/free p)))))

(defn load-style-default!
  "Reset to raygui's built-in style."
  []
  (gui-load-style-default)
  nil)

;; --- kwarg API: basics -------------------------------------------------------
;; raygui's C functions are positional and every one leads with a Rectangle;
;; these wrappers take :x :y :w :h plus named arguments so example code reads
;; self-descriptively. The raw bindings above remain the FFI boundary.
;;
;; Return convention: controls whose only outcome is "was it pressed" return a
;; boolean. Controls with an edit mode or a richer result return the raw
;; GuiResult int, so the caller can distinguish RESULT-CHANGED from
;; RESULT-TAB-CLOSE.

(defn label!
  "GuiLabel. :x :y :w :h :text."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 120
           h 24
           text ""}}]
  (gui-label (bounds! x y w h) text))

(defn button!
  "GuiButton. :x :y :w :h :text. True on the frame it is clicked."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 120
           h 30
           text "Button"}}]
  (= RESULT-PRESSED (gui-button (bounds! x y w h) text)))

(defn label-button!
  "GuiLabelButton. :x :y :w :h :text. True on the frame it is clicked."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 120
           h 24
           text "Label"}}]
  (= RESULT-PRESSED (gui-label-button (bounds! x y w h) text)))

(defn check-box!
  "GuiCheckBox. :x :y :w :h :text :cell (a :bool cell). True on change."
  [& {:keys [x y w h text cell]
      :or {x 0
           y 0
           w 20
           h 20
           text ""}}]
  (pos? (gui-check-box (bounds! x y w h) text (ptr cell))))

(defn toggle!
  "GuiToggle. :x :y :w :h :text :cell (a :bool cell). True on change."
  [& {:keys [x y w h text cell]
      :or {x 0
           y 0
           w 120
           h 30
           text "Toggle"}}]
  (pos? (gui-toggle (bounds! x y w h) text (ptr cell))))

(defn toggle-group!
  "GuiToggleGroup. :x :y :w :h :text :cell.

  :w and :h are the size of ONE button; raygui lays the group out from there.
  :text is semicolon-separated (\"ONE;TWO;THREE\") and :cell is an :int cell
  holding the selected index. True on change."
  [& {:keys [x y w h text cell]
      :or {x 0
           y 0
           w 80
           h 30
           text "ONE;TWO;THREE"}}]
  (pos? (gui-toggle-group (bounds! x y w h) text (ptr cell))))

(defn toggle-slider!
  "GuiToggleSlider. :x :y :w :h :text :cell.

  :text is semicolon-separated for the two ends (\"OFF;ON\") and :cell is an
  :int cell holding the active side. True on change."
  [& {:keys [x y w h text cell]
      :or {x 0
           y 0
           w 120
           h 30
           text "OFF;ON"}}]
  (pos? (gui-toggle-slider (bounds! x y w h) text (ptr cell))))

(defn line!
  "GuiLine, a horizontal separator. :x :y :w :h :text.

  An empty :text draws a plain rule; a non-empty one splits the rule around a
  caption. Named line! to match rl/line!, which is unrelated: this one is a
  raygui control and takes bounds, not endpoints."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 200
           h 12
           text ""}}]
  (gui-line (bounds! x y w h) text))

(defn status-bar!
  "GuiStatusBar. :x :y :w :h :text."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 200
           h 24
           text ""}}]
  (gui-status-bar (bounds! x y w h) text))

(defn dummy-rec!
  "GuiDummyRec, a placeholder box. :x :y :w :h :text."
  [& {:keys [x y w h text]
      :or {x 0
           y 0
           w 120
           h 30
           text "placeholder"}}]
  (gui-dummy-rec (bounds! x y w h) text))

(defn set-text-alignment!
  "Set TEXT_ALIGNMENT for one control (or DEFAULT for all).

  Style properties are global and persist across frames, so a control that
  changes alignment for itself must set it back afterwards or every later
  control inherits it."
  [control alignment]
  (gui-set-style control TEXT-ALIGNMENT alignment))

;; GuiIconName — a few of the 256 embedded 1-bit icons. Values read from
;; vendor/raygui.h; the full set is in that enum.
(def ^:const ICON-NONE          0)   (def ^:const ICON-FOLDER-OPEN 3)
(def ^:const ICON-FILE-SAVE     6)   (def ^:const ICON-OK-TICK     112)
(def ^:const ICON-CROSS         113) (def ^:const ICON-ARROW-RIGHT 115)
(def ^:const ICON-PLAYER-PLAY   131) (def ^:const ICON-GEAR        141)
(def ^:const ICON-BIN           143) (def ^:const ICON-STAR        157)
(def ^:const ICON-HEART         186) (def ^:const ICON-INFO        191)
(def ^:const ICON-WARNING       220)

(defn icon-text
  "GuiIconText — `text` with an icon marker prepended, for any control's :text.

  raygui renders a leading `#nnn#` in a control's text as icon nnn, so this is
  how an icon reaches a button: the button API is unchanged, only its text is."
  [icon-id text]
  (gui-icon-text icon-id text))

(defn draw-icon!
  "GuiDrawIcon, an icon drawn directly rather than inside a control.
  :icon :x :y :size :color."
  [& {:keys [icon x y size color]
      :or {icon ICON-NONE
           x 0
           y 0
           size 1
           color rl/DARKGRAY}}]
  (gui-draw-icon icon x y size color))

(defn set-icon-scale!
  "GuiSetIconScale. Global and persistent, like every style property."
  [scale]
  (gui-set-icon-scale scale))

;; --- kwarg API: inputs -------------------------------------------------------
;; Controls in this section have an EDIT MODE the application owns. raygui does
;; not remember whether a box is being edited: the control returns non-zero when
;; it wants the mode toggled, and the caller flips its own :bool cell. That is
;; the immediate-mode bargain, and it is why these wrappers return the raw
;; result rather than a boolean.

(defn text-box!
  "GuiTextBox. :x :y :w :h :cell (a :text cell) :edit? (a bool).

  Returns truthy when the box wants its edit mode toggled. raygui edits the
  cell's buffer IN PLACE, so the cell is both the input and the output."
  [& {:keys [x y w h cell edit?]
      :or {x 0
           y 0
           w 200
           h 32
           edit? false}}]
  (pos? (gui-text-box (bounds! x y w h) (ptr cell) (:size cell) (if edit? 1 0))))

(defn text-input-box!
  "GuiTextInputBox, a modal text prompt. :x :y :w :h :title :message :cell
  :buttons :result :secret.

  :cell is a :text cell for the entry, :result an :int cell receiving the index
  of the button pressed (-1 while none is), and :secret an optional :bool cell
  which, when supplied, adds a show/hide toggle and masks the entry.

  :buttons is semicolon-separated, the same convention as the toggle group.
  Returns the raw result."
  [& {:keys [x y w h title message cell buttons result secret]
      :or {x 0
           y 0
           w 320
           h 160
           title "Input"
           message ""
           buttons "Cancel;OK"}}]
  (gui-text-input-box (bounds! x y w h) title message
                      (ptr cell) (:size cell) buttons (ptr result)
                      (if secret (ptr secret) ffi/null)))
