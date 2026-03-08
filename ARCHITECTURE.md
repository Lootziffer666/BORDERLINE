# Borderline Architektur

Chat-ID: CH-20260308-01

## Primärversprechen
Borderline reduziert Android-Interaktionsreibung durch eine Accessibility-gestützte Overlay-Schicht.

## Harte Grenzen
- kein Duplikat von Zen, Smash, Reed oder Cathy
- kein allmächtiger Launcher-Ersatz
- keine unsichtbare Fremd-App-Manipulation jenseits normaler Accessibility-Grenzen
- keine sichere/geschützte Eingabedatenerfassung

## Gewünschte Grenzen, abstrahiert
Aus dem Projektkontext ableitbar:
- möglichst wenig Reibung
- modulare Testbarkeit statt Monolith
- transparente, leichte Oberfläche statt visuellem Betonklotz
- laterale Erweiterbarkeit ohne Architekturzerfall
- standalone nutzbar innerhalb der VENT-Familie

## Implementierte Modultrennung
### core
Gemeinsame Modelle, Modulschalter, State-Store, Logging.

### feature-accessibility
Erfasst Accessibility-Events und speist den gemeinsamen Status.

### feature-overlay
Zeigt Edge-Handle und Overlay-Panel als Accessibility-Overlay.

### feature-shortcuts
Stellt testbare Schnellaktionen für das Overlay bereit.

### app
Host-App mit Diagnose- und Toggle-Oberfläche.

## Aktivierungslogik
- Accessibility Backbone: verpflichtend
- Overlay Panel: schaltbar
- Quick Actions: schaltbar, aber logisch an Overlay gebunden
- Modulstatus wird mit Abhängigkeitsprüfung ausgewertet, nicht bloß als dumpfer Pref-Schalter

## Warum diese Form
So lässt sich jedes Verhalten isolierter prüfen:
- läuft der Accessibility-Dienst?
- erscheint das Overlay?
- reagieren Quick Actions?
- bleiben Abhängigkeiten nachvollziehbar?
- wirkt die Oberfläche leicht statt wie ein Backstein mit XML?
