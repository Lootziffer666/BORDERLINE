# Dogfood Alpha Test Plan — Borderline MVP

## Test Environment

- **Device:** Physical Android device (not emulator for overlay tests)
- **Minimum API:** 28 (Android 9)
- **Recommended:** API 33+ (Android 13+) for worst-case testing
- **Build:** Debug APK from main branch

## Pre-Test Checklist

- [ ] Device has "Draw over other apps" permission available
- [ ] Device is not in battery saver mode (kills foreground services)
- [ ] Default keyboard is not a custom IME that blocks overlays
- [ ] Gesture navigation mode noted (3-button vs swipe)

## Manual Test Matrix

### Gate 1-2: Foundation
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T01 | App installs | Install debug APK | Installs without error | |
| T02 | App icon opens provisioning | Tap app icon | ProvisioningActivity shows, not dashboard | |

### Gate 3: Storage Backbone
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T03 | Small snippet stored inline | Create snippet < 64 KB | Stored in metadata JSON | |
| T04 | Large snippet stored as file | Create snippet > 64 KB | File created in snippets/ dir | |
| T05 | Snippet survives app restart | Create snippet, force close, reopen | Snippet still visible | |
| T06 | No content truncation | Save 200 KB text, read back | All characters preserved | |

### Gate 4: Clipboard Intake
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T07 | URL classified | Copy a URL, open Clipboard+ | Shows as "URL" type | |
| T08 | Phone classified | Copy a phone number | Shows as "Phone number" | |
| T09 | Empty clipboard | Clear clipboard, open panel | Shows "Clipboard is empty" | |
| T10 | Android blocked (API 30+) | Copy in another app, switch away, open Borderline | Clear error or successful read | |

### Gate 5: Provisioning Screen
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T11 | Permission status correct | Check without overlay permission | Shows "Needs overlay permission" | |
| T12 | Grant permission | Tap "Grant" for overlay | Opens system settings, returns shows "Active" | |
| T13 | Module count updates | Grant overlay permission | "4 of 4 modules ready" | |
| T14 | Close hint visible | Scroll to bottom | "Close this screen" text visible | |

### Gate 6: Overlay Handles
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T15 | Handles appear | Start overlay from provisioning | 4 slim handles on screen edges | |
| T16 | Handles survive app switch | Switch to another app | Handles still visible | |
| T17 | Handle tap works | Tap any handle | Haptic feedback + action triggered | |
| T18 | Handle swipe works | Swipe from a handle | Same as tap | |
| T19 | Stop overlay | Tap "Stop Overlay" in provisioning | All handles removed | |
| T20 | Notification visible | Start overlay | "Borderline — Overlay handles active" notification | |
| T21 | Gesture nav conflict | Right handle + swipe-back gesture | Document if handle is reachable | |

### Gate 7: Snippet Manager
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T22 | Create snippet | Tap ＋, enter title + content, Save | Snippet appears in list | |
| T23 | Search snippet | Type in search field | List filters in real-time | |
| T24 | Copy snippet | Tap snippet row | "Copied full text" status + haptic | |
| T25 | Pin snippet | Tap "Pin" on a snippet | 📌 appears, snippet moves to top | |
| T26 | Edit snippet | Tap "Edit", change content, Save | Updated content preserved | |
| T27 | Delete snippet | Tap "Del" | Snippet removed from list | |
| T28 | Duplicate snippet | Tap "Dup" | Copy appears with "(copy)" suffix | |
| T29 | Empty state | Delete all snippets | "No snippets yet" message | |

### Gate 8: Paste/Insert
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T30 | Copy & paste flow | Copy snippet, switch to Notes app, long-press paste | Text appears in Notes | |
| T31 | Large content fallback | Copy 1 MB snippet | "File reference copied" status | |
| T32 | Status visible | Any copy operation | Green/blue/red status text, auto-hides in 3s | |

### Gate 9: QuickActions
| # | Test Case | Steps | Expected | Pass? |
|---|-----------|-------|----------|-------|
| T33 | URL actions | Copy URL, check suggestions | "Open in Browser" at top | |
| T34 | Phone actions | Copy phone number | "Call" + "SMS" suggested | |
| T35 | Email action | Copy email | "Send Email" suggested | |
| T36 | Address action | Copy German address | "Open in Maps" suggested | |
| T37 | Web search | Copy plain text | "Search Web" available | |
| T38 | Save always available | Any content type | "Save as Snippet" in actions | |

## Automated Tests (run before every build)

```bash
./gradlew :core:test
```

### Unit Test Inventory
| File | Tests | Gate |
|---|---|---|
| SnippetObjectStorageTest | 12 | Gate 3 |
| ContentClassifierTest | 8 | Gate 4 |
| PasteResultTest | 5 | Gate 8 |
| QuickActionRouterTest | 9 | Gate 9 |
| SnippetTest | (existing) | Pre-MVP |
| TransferItemTest | (existing) | Pre-MVP |
| ModuleRegistryTest | (existing) | Pre-MVP |
| **Total** | **34+** | |

## Dogfood Deployment

1. Build debug APK: `./gradlew :app:assembleDebug`
2. Install via ADB: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
3. Open app → Grant overlay permission → Start overlay
4. Use for 1 day, note any issues in KNOWN_BUGS.md

## Issue Reporting Template

```
### [KB-XX] Title
- **Severity:** Critical / High / Medium / Low
- **Affected:** API level, device, OEM
- **Steps to reproduce:**
  1. ...
  2. ...
- **Expected:** ...
- **Actual:** ...
- **Workaround:** ...
- **Gate:** ...
```
