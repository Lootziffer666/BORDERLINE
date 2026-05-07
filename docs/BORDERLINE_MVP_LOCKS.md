# Borderline MVP Scope Locks

These constraints are hard locks for the MVP. No gate may violate them.

## Hard Locks

### 1. No Keyboard / IME Integration
Borderline does not build, embed, or ship a custom keyboard or IME.
No `InputMethodService`. No keyboard layout. No key injection.

### 2. No Accessibility Service in MVP
Borderline MVP does not use `AccessibilityService`.
No `TYPE_ACCESSIBILITY_OVERLAY`.
No accessibility event listening.
No node tree inspection.
Overlay uses `TYPE_APPLICATION_OVERLAY` via draw-over-apps permission only.

### 3. No Embedded Third-Party Widgets
No embedding of other apps' widgets, views, or remote views.
No AppWidgetHost. No RemoteViews from third parties.

### 4. Overlay Only via Draw-Over-Apps
All overlay surfaces use `SYSTEM_ALERT_WINDOW` permission.
Window type: `TYPE_APPLICATION_OVERLAY`.
Permission request flow: `Settings.canDrawOverlays()` check + intent to `ACTION_MANAGE_OVERLAY_PERMISSION`.

### 5. Snippets/Artifacts: No Artificial Size Cap Below 100 MB
Snippets and artifacts must not be artificially limited below 100 MB.
100 MB is the MVP test boundary, not a UI-enforced limit.
Large content is stored as files, not crammed into SharedPreferences.

### 6. Never Silently Truncate
Copied or saved content must never be silently truncated.
If full clipboard copy fails, the user sees a clear fallback status:
- "Copied full text"
- "Copied file reference"
- "Saved but not copied"
- "Clipboard failed"
- "Needs manual action"

### 7. App Icon Opens Provisioning Only
The launcher icon opens a provisioning/setup screen.
Daily use happens through overlay handles and contextual surfaces.
No full dashboard. No app-world. No ZenOS overload.

### 8. Clipboard File/Reference Fallback
When clipboard content exceeds Android's clipboard size limit or
the system blocks access, Borderline switches to a visible file-link
or reference fallback. No silent data loss.

## Scope Boundaries

### What Borderline MVP IS
- A draw-over-apps overlay with edge handles
- A snippet manager (store, search, copy, paste-assist)
- A clipboard intake tool (read, classify, store, confirm)
- A shortcut/handoff launcher (intents, app opens, shares)
- A context router for quick actions (pattern → suggestion)
- A provisioning screen for setup and module management

### What Borderline MVP IS NOT
- Not a launcher
- Not an AI product (no ML/AI required)
- Not an accessibility automation tool
- Not a full dashboard app
- Not a Tasker replacement
- Not a file manager
- Not a note-taking app

## Enforcement

Every gate must verify:
1. No accessibility service code is activated or required
2. No IME code is added
3. No third-party widget embedding
4. Overlay windows use TYPE_APPLICATION_OVERLAY
5. No silent truncation anywhere
6. App icon leads to provisioning, not a dashboard
