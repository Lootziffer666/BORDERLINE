# 🚪 GATES — BORDERLINE

> MVP Alpha (Gates 1–10) abgeschlossen.

---

## 🔜 Nächste Gates

### Gate BL-011: Overlay-Permissions-Manager
- **Branch:** `gate/bl-011-overlay-permissions`
- **Ziel:** Robuste Permission-Verwaltung für Overlay-Panels
- **To-Dos:**
  - [ ] SYSTEM_ALERT_WINDOW Permission-Flow
  - [ ] Fallback bei verweigerte Permission
  - [ ] Auto Re-Check bei App-Start
  - [ ] User-freundliche Erklärung
- **Akzeptanz:** Overlay funktioniert nach Neustart
- **Kill:** Permission ohne Erklärung erzwingen

### Gate BL-012: Snippet-Synchronisation
- **Branch:** `gate/bl-012-snippet-sync`
- **Ziel:** Snippets zwischen Geräten synchronisieren (lokal)
- **To-Dos:**
  - [ ] Export/Import als JSON
  - [ ] QR-Code-basierter Transfer
  - [ ] Merge-Strategie bei Konflikten
  - [ ] Backup/Restore
- **Akzeptanz:** Transfer ohne Internet
- **Kill:** Cloud-Sync-Zwang

### Gate BL-013: Panel-Theming
- **Branch:** `gate/bl-013-panel-themes`
- **Ziel:** Anpassbare Panel-Darstellung
- **To-Dos:**
  - [ ] CSS-Variablen für Theming
  - [ ] Light/Dark per System-Setting
  - [ ] Custom Farbschemata
  - [ ] Font-Größen (Accessibility)
- **Akzeptanz:** Panels passen sich visuell an
- **Kill:** Hardcoded Styles

### Gate BL-014: Multi-Panel-Layout
- **Branch:** `gate/bl-014-multi-panel`
- **Ziel:** Mehrere Panels gleichzeitig
- **To-Dos:**
  - [ ] Panel-Stacking mit Drag-to-Reorder
  - [ ] Split-View für Tablets
  - [ ] Minimierung / Wiederherstellung
  - [ ] Memory-Management für Panel-Pool
- **Akzeptanz:** 3+ Panels stabil
- **Kill:** Crash bei > 2 Panels

### Gate BL-015: Tasker-Variable-Bridge
- **Branch:** `gate/bl-015-tasker-bridge`
- **Ziel:** Bidirektionale Variable-Brücke zu Tasker
- **To-Dos:**
  - [ ] JS→Tasker Variable-Push
  - [ ] Tasker→Panel Variable-Injection
  - [ ] Event-basiert (kein Polling)
  - [ ] Debug-Console
- **Akzeptanz:** Variablen fließen in beide Richtungen
- **Kill:** Polling-basiert
