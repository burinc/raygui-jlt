(ns net.b12n.raygui-jlt.check
  "Headless compile-check (`joltc -M:check`).

  Requires every example namespace, which compiles each one (macro-expansion,
  var resolution, arity checks) WITHOUT opening a window — so the whole suite can
  be verified with no display attached.

  It does NOT exercise rendering, and for a GUI toolkit that gap is the whole
  point of the screenshot gate: a control drawn at the wrong bounds, or a style
  that never loaded, compiles perfectly and passes this check. See each example's
  RAYGUI_APP_AUTO_QUIT_MS + RAYGUI_APP_SHOT smoke."
  (:require
   [net.b12n.raygui-jlt.basic-controls]
   [net.b12n.raygui-jlt.combo-box]
   [net.b12n.raygui-jlt.dropdown-box]
   [net.b12n.raygui-jlt.floating-window]
   [net.b12n.raygui-jlt.icon-buttons]
   [net.b12n.raygui-jlt.labels-lines]
   [net.b12n.raygui-jlt.list-view]
   [net.b12n.raygui-jlt.list-view-ex]
   [net.b12n.raygui-jlt.panel-group-box]
   [net.b12n.raygui-jlt.progress-bar]
   [net.b12n.raygui-jlt.raygui]
   [net.b12n.raygui-jlt.raylib]
   [net.b12n.raygui-jlt.scroll-panel]
   [net.b12n.raygui-jlt.sliders]
   [net.b12n.raygui-jlt.spinner-value-box]
   [net.b12n.raygui-jlt.tab-bar]
   [net.b12n.raygui-jlt.text-box]
   [net.b12n.raygui-jlt.text-input-box]
   [net.b12n.raygui-jlt.toggles]
   [net.b12n.raygui-jlt.window-box]))

(defn -main
  [& _]
  (println "net.b12n.raygui-jlt: all example namespaces compiled OK"))
