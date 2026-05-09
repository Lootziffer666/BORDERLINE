<h1 align="center">🔲 BORDERLINE</h1>

<p align="center">
  <strong>Tasker App Factory — Lokale Panel-Schicht für Android</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=flat-square" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=flat-square" />
  <img src="https://img.shields.io/badge/MVP-Alpha%20Complete-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Gates-10%20Done-brightgreen?style=flat-square" />
</p>

---

## 🧭 Was ist BORDERLINE?

BORDERLINE ist ein **Tasker-App-Factory-Prototyp** — eine lokale HTML-Panel-Schicht für Android mit Kotlin/Compose-UI und HTML-WebView-Panels.

Kein natives Android-Projekt im klassischen Sinne, kein Launcher, kein IME, keine Accessibility-Abhängigkeit.

---

## 🏗 Architektur

```
BORDERLINE/
├── app/                    # Android Application Shell
├── core/                   # Shared Kotlin core
├── feature-accessibility/  # Accessibility features
├── feature-overlay/        # Overlay panel system
├── feature-shortcuts/      # Shortcut management
├── borderline_taf/         # Tasker App Factory HTML panels
├── Snippets/               # Code snippet library
└── docs/                   # Dokumentation
```

---

## ⚡ Quickstart

1. Dateien auf Handy kopieren
2. In Tasker: **Scene** mit **WebView** anlegen
3. `borderline_taf/index.html` in WebView laden
4. Start-Task bauen
5. Mit **Tasker App Factory** als APK exportieren

---

## 📚 Docs

| Datei | Inhalt |
|-------|--------|
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | System-Architektur |
| [`MVP_GAP_ANALYSIS.md`](MVP_GAP_ANALYSIS.md) | MVP-Lückenanalyse |
| [`TASKER_SETUP.md`](TASKER_SETUP.md) | Setup-Anleitung |

---

## 🚪 Gates

MVP Alpha (10 Gates) abgeschlossen. Siehe [`GATES.md`](GATES.md).

---

## 🔗 Relevante Projekte

| Projekt | Relevanz |
|---------|----------|
| [AAswordman/Operit](https://github.com/AAswordman/Operit) | Android Automation Agent |
| [gkd-kit/gkd](https://github.com/gkd-kit/gkd) | Accessibility-basierte Automatisierung |

---

<p align="center"><em>Panels statt Apps. Lokal statt Cloud. Tasker-native.</em></p>
