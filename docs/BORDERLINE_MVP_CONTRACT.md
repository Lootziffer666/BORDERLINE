# Borderline MVP Contract

Version: 1.0
Date: 2026-05-07

## Overview

Borderline MVP ships with exactly 4 module slots. These are not tiles, not tabs,
not a grid layout. They are functional domains with clear boundaries.

The MVP proves that Borderline can reduce Android interaction friction through
a lightweight draw-over-apps overlay — without Accessibility Service, without IME,
without third-party widget embedding.

---

## Module 1: Snippets

### Purpose
Store, search, copy, and paste text artifacts (prompts, templates, code, markdown).

### Input
- User-created text (manual entry)
- Pasted/imported text
- Files (markdown, text, code)

### Output
- Clipboard copy (full text or file reference)
- Share intent
- File export

### Boundaries
- No AI-powered generation
- No auto-categorization in MVP
- No sync/cloud in MVP
- No rich text editor (plain text + markdown preview)
- Large content stored as files, not inline
- Never silently truncate

### States
| State | Meaning |
|---|---|
| `disabled` | Module is off, no UI surface |
| `configured` | Module is set up, ready to activate |
| `active` | Module is running, handles respond to gestures |
| `failed` | Module encountered an error, shows diagnostic info |

---

## Module 2: Clipboard+

### Purpose
Capture, classify, store, and provide status for clipboard content.

### Input
- System clipboard (text, URLs, images if accessible)
- Manual paste into Borderline

### Output
- Status confirmation (what was captured, what type)
- Stored clipboard history
- Re-copy to clipboard
- File reference for large content

### Boundaries
- Cannot guarantee background clipboard monitoring (Android restriction)
- Must work with foreground/focus limitations
- Image/file support is best-effort, not guaranteed
- No silent background snooping
- Clear error when Android blocks clipboard access

### States
| State | Meaning |
|---|---|
| `disabled` | Module is off |
| `configured` | Ready, waiting for clipboard events |
| `active` | Monitoring/processing clipboard |
| `failed` | Android blocked access, permission revoked |

---

## Module 3: Shortcuts / Handoffs

### Purpose
Launch defined actions: open apps, trigger shares, fire intents.

### Input
- User-configured shortcut definitions
- Context from QuickActions suggestions

### Output
- App launch via `PackageManager.getLaunchIntentForPackage()`
- Share via `ACTION_SEND`
- Dial via `ACTION_DIAL`
- View via `ACTION_VIEW`
- Custom intent

### Boundaries
- No embedded third-party widgets
- No RemoteViews
- No AppWidgetHost
- Actions are explicit, not automatic
- Cannot inject text into other apps (no Accessibility, no IME)
- Handoff = open target app + copy to clipboard, user pastes

### States
| State | Meaning |
|---|---|
| `disabled` | No shortcuts configured |
| `configured` | Shortcuts defined, ready to fire |
| `active` | Shortcut panel available via handle |
| `failed` | Target app not installed, intent failed |

---

## Module 4: QuickActions

### Purpose
Recognize simple content patterns and suggest matching handoffs.

### Input
- Clipboard content
- Snippet content
- User-selected text (if shared to Borderline)

### Output
- Suggestion chips (e.g., "Looks like an address → Maps / Save")
- Handoff to Shortcuts module
- Save to Snippets module

### Pattern Recognition (v0 — heuristic, no AI)

| Pattern | Detection | Suggestions |
|---|---|---|
| URL | Regex: `https?://...` | Browser / Save / Share |
| Address | Heuristic: street + city patterns | Maps / Save |
| Phone number | Regex: `+XX...` or local patterns | Dial / Contact / Save |
| Amount/Invoice | Regex: `€/$/£` + number | Calculator / Save |
| Recipe text | Keyword: Zutaten, ingredients, etc. | Shopping list / Save |
| Calendar text | Keywords: date, time, Termin, meeting | Calendar / Save |
| Long text | Length > 500 chars | Save as Snippet / File |
| Image | MIME type check | Save / Share / Note |
| File | URI scheme check | Save / Open / Share |

### Boundaries
- No AI/ML required
- No app content inspection beyond clipboard/shared data
- Suggestions only, never auto-execute
- False positives must be dismissable
- No app-internal scraping

### States
| State | Meaning |
|---|---|
| `disabled` | No context routing |
| `configured` | Patterns loaded |
| `active` | Analyzing incoming content |
| `failed` | Pattern engine error |

---

## Module State Machine

```
[disabled] ──enable──▶ [configured] ──activate──▶ [active]
     ▲                      ▲                        │
     │                      │                        │
     └──────disable─────────┘──────error─────▶ [failed]
                                                     │
                                                     │
                                              recover─┘
```

All modules follow the same state lifecycle.
Module transitions are explicit — no silent state changes.

---

## Non-Goals (MVP)

### Explicitly Not In Scope

1. **No Launcher** — Borderline does not replace or wrap the home screen
2. **No AI Requirement** — All features work with heuristics and user input
3. **No Accessibility Hack** — Zero `AccessibilityService` usage
4. **No Full Automation Claims** — Android restricts what overlay apps can do; Borderline is honest about it
5. **No ZenOS Vision Dependency** — Every module must work standalone, not as part of a future ecosystem
6. **No Cloud/Sync** — All data is local in MVP
7. **No Multi-Device** — Single device only
8. **No Rich Text Editor** — Plain text with optional markdown rendering
9. **No Background Services Beyond Overlay** — No persistent background processing

---

## Architecture Constraint

Every module slot has:
- A clear purpose (one sentence)
- Defined input types
- Defined output types
- Explicit boundaries (what it does NOT do)
- A 4-state lifecycle (disabled → configured → active → failed)

Nothing depends on a future ZenOS vision.
Borderline must be small enough to actually build and ship.

---

## Success Criteria

The MVP is successful when:
1. A user can store and retrieve text snippets via overlay handles
2. A user can see what's in their clipboard and get clear status
3. A user can launch shortcuts/apps from the overlay
4. A user gets simple pattern-based suggestions for content
5. All of this works without Accessibility Service
6. All of this works via draw-over-apps overlay
7. Nothing is silently truncated or lost
8. The app icon opens setup, not a dashboard
