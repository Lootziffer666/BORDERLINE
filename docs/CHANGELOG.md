# Changelog — Borderline Android

## v0.5.0-alpha (MVP Gates 1–10)

### Added
- **Gate 1:** Repository documentation (BORDERLINE_CURRENT_STATE.md, BORDERLINE_MVP_LOCKS.md)
- **Gate 2:** MVP Contract — 4 module slots with states and boundaries
- **Gate 3:** Storage Backbone — SnippetObject data model with file-backed storage
  - Inline storage for < 64 KB, file storage for larger content
  - SHA-256 checksums, no silent truncation
  - 12 unit tests including 100 MB boundary
- **Gate 4:** Clipboard Intake Mode 1 — Read, classify, store
  - ContentClassifier: URL, phone, email, address, amount, calendar detection
  - Android API-aware access info (API 26–33+)
  - 8 unit tests
- **Gate 5:** Provisioning Screen — App icon opens setup, not dashboard
  - Module status overview, permission management
  - Start/Stop overlay controls
- **Gate 6:** Overlay Handles v0 — Draw-over-apps edge strips
  - TYPE_APPLICATION_OVERLAY (no Accessibility Service)
  - 4 positions, swipe/tap activation, haptic feedback
  - Keyboard-aware handle shifting
- **Gate 7:** Snippet Manager v0 — Full CRUD overlay panel
  - Create, edit, delete, duplicate snippets
  - Real-time search, pin/unpin, recently used sorting
  - File reference fallback for large content
- **Gate 8:** Paste/Insert Mode 1 — Honest copy & return flow
  - No IME, no Accessibility Service
  - Clear status feedback for every operation
  - 5 unit tests
- **Gate 9:** QuickActions v0 — Context-aware action router
  - Heuristic pattern matching, intent dispatch
  - 10 action types (call, SMS, email, maps, etc.)
  - 9 unit tests
- **Gate 10:** Dogfood Alpha — Test plan, known bugs, manual test matrix

### Hard MVP Locks (unchanged)
- No IME/keyboard integration
- No Accessibility Service
- No embedded third-party widgets
- Overlay only via draw-over-apps
- No artificial content size caps
- Never silently truncate
- If clipboard fails → visible fallback
- App icon → provisioning, not dashboard
