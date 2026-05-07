# Known Bugs & Limitations — Borderline MVP Alpha

> This file is honest. Every known issue is documented here.
> If something breaks, add it. Never hide a bug.

## Critical (Blocks Core Usage)

| # | Bug | Affected | Workaround | Gate |
|---|-----|----------|------------|------|
| KB-01 | Overlay handles may conflict with Android gesture navigation (back swipe) on right edge | API 29+ | Use left-side handles or disable gesture nav in system settings | Gate 6 |
| KB-02 | Clipboard reading fails silently on API 30+ if app loses focus during read | API 30+ | Keep Borderline panel in foreground while reading clipboard | Gate 4 |

## High (Impacts UX)

| # | Bug | Affected | Workaround | Gate |
|---|-----|----------|------------|------|
| KB-03 | Some OEMs auto-clear clipboard after 30–60 seconds | Samsung, OPPO | Paste quickly after copy, or use Save to Snippet instead | Gate 8 |
| KB-04 | foregroundServiceType="specialUse" may require Play Store justification | All | OK for sideloading/dogfood; needs review before Play Store submission | Gate 6 |
| KB-05 | Keyboard height detection for handle shift is unreliable on some devices | OEM-specific | Lower handles may overlap keyboard on some devices | Gate 6 |
| KB-06 | resolveActivity() returns null on API 30+ without <queries> in manifest | API 30+ | Add <queries> declarations for intent actions | Gate 9 |

## Medium (Cosmetic / Minor)

| # | Bug | Affected | Workaround | Gate |
|---|-----|----------|------------|------|
| KB-07 | Handle alpha transparency renders differently on AMOLED vs LCD | Hardware | Adjust HANDLE_ALPHA constant per device class | Gate 6 |
| KB-08 | Long snippet titles truncated without ellipsis in some cases | UI | Keep titles under 40 chars | Gate 7 |
| KB-09 | German-only address pattern matching (Straße, Platz, etc.) | i18n | Add patterns for other locales | Gate 4/9 |
| KB-10 | Calendar intent opens without pre-filled date/time | All | User must fill in date/time manually | Gate 9 |

## By Design (Not Bugs)

| # | Behavior | Reason |
|---|----------|--------|
| BD-01 | No text injection into other apps | MVP lock: no IME, no Accessibility Service |
| BD-02 | No background clipboard listener | API 30+ blocks it; explicit user action required |
| BD-03 | App icon opens provisioning, not daily-use UI | Design decision: daily use via overlay handles |
| BD-04 | No embedded third-party widgets | MVP lock |
| BD-05 | No cloud sync | MVP lock: local-only storage |
