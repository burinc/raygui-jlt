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
