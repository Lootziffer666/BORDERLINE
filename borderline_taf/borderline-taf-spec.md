# BorderlineTAF v0.2 – Spec

## Zweck
BorderlineTAF ist ein **hässlicher Funktionsprototyp**, um eine lokale Panel-Schicht für Tasker App Factory zu testen.

## Scope
- Snippets verwalten und kopieren.
- Status „Bereit zum Einfügen“ anzeigen.
- AI-Ziele als URLs öffnen.
- DOC-Ordnungstags erzeugen.
- Capture-Text als FLOWINPUT-Datei vorbereiten.
- Große Artefakte bis 100 MB als Datei behandeln.

## Nicht-Ziele
- Keine native Android-App.
- Kein Homescreen-Ersatz.
- Kein App Drawer.
- Keine Tastatur/IME.
- Keine Accessibility-/AutoInput-Abhängigkeit.
- Kein Widget-Hosting fremder Widgets.
- Kein Auto-Paste in fremde Felder.

## Copy-Semantik
1. **Full Text Copy**: Volltext im Clipboard.
2. **File Reference Copy**: Volltext als Datei gespeichert, Clipboard enthält Dateiverweis.
3. **Saved Only**: Nur gespeichert, nicht kopiert.

## Size Policy
- Keine künstliche Size-Cap unter 100 MB.
- Kein stilles Abschneiden.
- Clipboard wird immer zuerst versucht.
- Bei Fehlschlag oder ungeeigneter Größe: Datei + Dateiverweis-Clipboard.

## Paste Mode 1
- Snippet auswählen → Clipboard setzen → Nutzer fügt manuell ein.
- Kein AutoInput, kein Accessibility, kein automatisches Einfügen.
