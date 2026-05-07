# Borderline Current State

Generated: 2026-05-07
Repo: Lootziffer666/BorderlineAndroid
Branch: main
Commit: 8e5a55e

## Repository Overview

Borderline is an Android app project structured as a multi-module Gradle build.
The current codebase contains two parallel approaches:

1. **Native Android App** (Kotlin, multi-module Gradle) — the primary codebase
2. **Tasker App Factory prototype** (`borderline_taf/`) — an HTML-based Tasker WebView panel

### Current Architecture (Native)

| Module | Purpose |
|---|---|
| `app` | Host app with MainActivity → SetupWizard |
| `core` | Shared models, module system, state store, logging |
| `feature-accessibility` | Accessibility service, context analyzer |
| `feature-overlay` | Edge handles, overlay panels, IME detection |
| `feature-shortcuts` | QuickAction registry |

### Build Configuration

- **AGP:** 9.1.0
- **Kotlin:** 2.3.10
- **compileSdk:** 36
- **minSdk:** 28
- **targetSdk:** 36
- **Package:** `de.lootz.borderline`
- **Version:** 0.4.2 (versionCode 6)

### Key Technical Decisions Already Made

- Accessibility-based overlay (TYPE_ACCESSIBILITY_OVERLAY) — **will change in MVP**
- WindowManager for overlay rendering
- Edge swipe detection for panel activation
- IME-aware panel repositioning
- JSON-based SharedPreferences for data persistence
- Haptic feedback on interactions

## Root File Tree

### Android/Kotlin Source (35 files)
```
./Snippets/01_borderline_smart_content_datenmodelle.kt
./app/src/main/java/de/lootz/borderline/MainActivity.kt
./app/src/main/kotlin/com/borderline/app/SetupWizard.kt
./core/src/main/java/de/lootz/borderline/core/AccessibilitySnapshot.kt
./core/src/main/java/de/lootz/borderline/core/AccessibilityStateStore.kt
./core/src/main/java/de/lootz/borderline/core/BorderlineLogger.kt
./core/src/main/java/de/lootz/borderline/core/ClipboardGrabber.kt
./core/src/main/java/de/lootz/borderline/core/DeviceCompatibility.kt
./core/src/main/java/de/lootz/borderline/core/JsonSnippetRepository.kt
./core/src/main/java/de/lootz/borderline/core/JsonTransferItemRepository.kt
./core/src/main/java/de/lootz/borderline/core/ModuleDescriptor.kt
./core/src/main/java/de/lootz/borderline/core/ModuleId.kt
./core/src/main/java/de/lootz/borderline/core/ModulePrefs.kt
./core/src/main/java/de/lootz/borderline/core/ModuleRegistry.kt
./core/src/main/java/de/lootz/borderline/core/Snippet.kt
./core/src/main/java/de/lootz/borderline/core/SnippetRepository.kt
./core/src/main/java/de/lootz/borderline/core/TransferItem.kt
./core/src/main/java/de/lootz/borderline/core/TransferItemRepository.kt
./core/src/main/kotlin/com/borderline/core/models/ContentModels.kt
./core/src/main/kotlin/com/borderline/core/repository/QuickActionRepository.kt
./feature-accessibility/src/main/kotlin/com/borderline/feature/accessibility/BorderlineAccessibilityService.kt
./feature-accessibility/src/main/kotlin/com/borderline/feature/accessibility/ContextAnalyzer.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/BorderlineOverlayController.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/EdgeSwipeDetector.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/Extensions.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/ImeStateDetector.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/LayoutModels.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/ClipboardPanel.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/QuickActionsPanel.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/ShortcutsPanel.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/SmartContentAdapter.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/SmartPanel.kt
./feature-overlay/src/main/kotlin/com/borderline/feature/overlay/panels/SnippetsPanel.kt
./feature-shortcuts/src/main/java/de/lootz/borderline/feature/shortcuts/QuickAction.kt
./feature-shortcuts/src/main/java/de/lootz/borderline/feature/shortcuts/QuickActionRegistry.kt
```

### Test Files (3 files)
```
./core/src/test/java/de/lootz/borderline/core/ModuleRegistryTest.kt
./core/src/test/java/de/lootz/borderline/core/SnippetTest.kt
./core/src/test/java/de/lootz/borderline/core/TransferItemTest.kt
```

### Android Resources (29 files)
```
./app/src/main/res/drawable/ic_launcher_foreground.xml
./app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
./app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml
./app/src/main/res/values/colors.xml
./app/src/main/res/values/strings.xml
./app/src/main/res/values/themes.xml
./core/src/main/res/drawable/ic_accessibility.xml
./core/src/main/res/drawable/ic_add.xml
./core/src/main/res/drawable/ic_battery.xml
./core/src/main/res/drawable/ic_block.xml
./core/src/main/res/drawable/ic_check_circle.xml
./core/src/main/res/drawable/ic_close.xml
./core/src/main/res/drawable/ic_content_copy.xml
./core/src/main/res/drawable/ic_delete.xml
./core/src/main/res/drawable/ic_edit.xml
./core/src/main/res/drawable/ic_grid_view.xml
./core/src/main/res/drawable/ic_help.xml
./core/src/main/res/drawable/ic_info.xml
./core/src/main/res/drawable/ic_layers.xml
./core/src/main/res/drawable/ic_more_horiz.xml
./core/src/main/res/drawable/ic_pip.xml
./core/src/main/res/drawable/ic_power.xml
./core/src/main/res/drawable/ic_push_pin.xml
./core/src/main/res/drawable/ic_remove_circle.xml
./core/src/main/res/drawable/ic_search.xml
./core/src/main/res/drawable/ic_tune.xml
./core/src/main/res/values/colors.xml
./feature-accessibility/src/main/res/values/strings.xml
./feature-accessibility/src/main/res/xml/borderline_accessibility_service.xml
```

### Gradle/Build (12 files)
```
./app/build.gradle.kts
./build.gradle.kts
./core/build.gradle.kts
./feature-accessibility/build.gradle.kts
./feature-overlay/build.gradle.kts
./feature-shortcuts/build.gradle.kts
./gradle.properties
./gradle/wrapper/gradle-wrapper.jar
./gradle/wrapper/gradle-wrapper.properties
./gradlew
./gradlew.bat
./settings.gradle.kts
```

### Documentation (14 files)
```
./AGENTS.md
./ARCHITECTURE.md
./CLAUDE.md
./MVP_GAP_ANALYSIS.md
./NOT_IMPLEMENTED.md
./README.md
./Snippets/02_borderline_context_analyzer_und_kontexttypen.md
./Snippets/03_borderline_repository_smart_ranking_und_persistenz.md
./Snippets/04_borderline_overlay_grundsystem_und_gestensteuerung.md
./Snippets/04_borderline_setup_wizard_und_onboarding.md
./Snippets/05_borderline_accessibility_bar_layout_ime_und_panelpositionierung.md
./Snippets/05_borderline_smart_panel_edit_mode_und_plus_button.md
./Snippets/06_borderline_panel_implementierungen_und_service_backbone.md
./TASKER_SETUP.md
```

### Tasker/TAF Prototype (6 files)
```
./borderline_taf/borderline-taf-spec.md
./borderline_taf/doc-tags.json
./borderline_taf/index.html
./borderline_taf/knownbugs.md
./borderline_taf/room-map.json
./borderline_taf/snippets.json
```

### Design Snippets (0 files)
```

```

### Other (31 files)
```
./.gitattributes
./.github/workflows/build-debug-apk.yml
./.gitignore
./.idea/.gitignore
./.idea/.name
./.idea/AndroidProjectSystem.xml
./.idea/caches/deviceStreaming.xml
./.idea/compiler.xml
./.idea/deploymentTargetSelector.xml
./.idea/gradle.xml
./.idea/kotlinc.xml
./.idea/markdown.xml
./.idea/migrations.xml
./.idea/misc.xml
./.idea/runConfigurations.xml
./.idea/vcs.xml
./app/proguard-rules.pro
./app/src/main/AndroidManifest.xml
./borderline_kotlin_files.zip
./core/consumer-rules.pro
./core/proguard-rules.pro
./core/src/main/AndroidManifest.xml
./feature-accessibility/consumer-rules.pro
./feature-accessibility/proguard-rules.pro
./feature-accessibility/src/main/AndroidManifest.xml
./feature-overlay/consumer-rules.pro
./feature-overlay/proguard-rules.pro
./feature-overlay/src/main/AndroidManifest.xml
./feature-shortcuts/consumer-rules.pro
./feature-shortcuts/proguard-rules.pro
./feature-shortcuts/src/main/AndroidManifest.xml
```

## Relevant Android/Tasker/TAF Files

### Core Data Models
| File | Purpose |
|---|---|
| `core/.../Snippet.kt` | Basic snippet: id, title, content, category, createdAt |
| `core/.../TransferItem.kt` | Clipboard item: id, kind (TEXT/URI), label, preview, timestamp, pinned |
| `core/.../ClipboardGrabber.kt` | On-demand clipboard reader (preview capped at 200 chars) |
| `core/.../ContentModels.kt` | Smart content models for overlay panels (SnippetContent, ClipboardContent, etc.) |

### Module System
| File | Purpose |
|---|---|
| `core/.../ModuleId.kt` | Module enum: ACCESSIBILITY, OVERLAY, SHORTCUTS |
| `core/.../ModuleRegistry.kt` | Module dependency graph (Accessibility → Overlay → Shortcuts) |
| `core/.../ModuleDescriptor.kt` | Module metadata with dependencies |
| `core/.../ModulePrefs.kt` | SharedPreferences-based module state |

### Overlay System
| File | Purpose |
|---|---|
| `feature-overlay/.../BorderlineOverlayController.kt` | Main overlay controller with 4 panels |
| `feature-overlay/.../EdgeSwipeDetector.kt` | Edge swipe gesture detection |
| `feature-overlay/.../ImeStateDetector.kt` | Keyboard visibility detection |
| `feature-overlay/.../LayoutModels.kt` | Screen dimensions, panel positions, AccessibilityBarLayout |
| `feature-overlay/.../panels/SnippetsPanel.kt` | Snippet list panel |
| `feature-overlay/.../panels/ClipboardPanel.kt` | Clipboard history panel |
| `feature-overlay/.../panels/ShortcutsPanel.kt` | Shortcuts panel |
| `feature-overlay/.../panels/QuickActionsPanel.kt` | Quick actions panel |
| `feature-overlay/.../panels/SmartPanel.kt` | Base panel interface |
| `feature-overlay/.../panels/SmartContentAdapter.kt` | Content adapter for panels |

### Accessibility
| File | Purpose |
|---|---|
| `feature-accessibility/.../BorderlineAccessibilityService.kt` | Main accessibility service |
| `feature-accessibility/.../ContextAnalyzer.kt` | Analyzes accessibility events for context |

### App Entry
| File | Purpose |
|---|---|
| `app/.../MainActivity.kt` | Launcher activity → SetupWizard |
| `app/.../SetupWizard.kt` | Setup/onboarding flow |

### Tasker/TAF
| File | Purpose |
|---|---|
| `borderline_taf/index.html` | TAF HTML panel prototype |
| `borderline_taf/borderline-taf-spec.md` | TAF specification |
| `borderline_taf/snippets.json` | TAF snippet data |
| `borderline_taf/room-map.json` | TAF room mapping |
| `borderline_taf/doc-tags.json` | TAF document tags |
| `borderline_taf/knownbugs.md` | TAF known bugs |

## Existing Tests (3 files, all in core)

- `ModuleRegistryTest.kt` — Module dependency chain validation
- `SnippetTest.kt` — Snippet creation/equality
- `TransferItemTest.kt` — TransferItem creation/equality

## Critical Observations

1. **Overlay uses TYPE_ACCESSIBILITY_OVERLAY** — requires Accessibility Service. MVP must switch to `TYPE_APPLICATION_OVERLAY` (draw-over-apps permission).
2. **ClipboardGrabber truncates** preview to 200 chars — full content is not stored.
3. **Snippet model is minimal** — no storageMode, no file-backed storage, no size handling.
4. **Module system depends on Accessibility** as root requirement.
5. **Two package namespaces** coexist: `de.lootz.borderline` (app, core) and `com.borderline` (feature modules).
6. **borderline_kotlin_files.zip** exists at root — provenance unclear.
7. **No CI/CD pipeline** for actual builds (GitHub Actions workflow exists but may not be functional).
