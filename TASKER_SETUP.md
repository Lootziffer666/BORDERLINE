# Tasker Setup für BorderlineTAF v0.2

## Ziel
Lokale HTML-Oberfläche in einer Tasker-Scene nutzen und später mit Tasker App Factory exportieren.

## Minimaler Aufbau
1. Ordner `borderline_taf` auf das Gerät kopieren (z. B. interner Speicher).
2. In Tasker eine Scene mit WebView erstellen.
3. WebView-Quelle auf lokale `index.html` setzen.
4. Start-Task erstellen, der die Scene öffnet.
5. Optional Buttons mit Tasker-Actions verbinden (z. B. Variable setzen, Datei schreiben).

## Clipboard-Reihenfolge (MVP)
1. Zuerst Clipboard direkt versuchen.
2. Wenn fehlschlägt oder bei großen Inhalten unpraktisch: als Datei speichern.
3. Dateiverweis ins Clipboard legen.
4. Status klar anzeigen: `Dateiverweis kopiert`.

## Capture → FLOWINPUT mit Tasker Write File
Empfohlener späterer Tasker-Flow:
1. Text aus Capture-Feld als Variable übernehmen.
2. Dateiname erzeugen: `FLOWINPUT_YYYYMMDD_NNNNN.txt`.
3. Zielpfad setzen, z. B. `/Documents/FLOWINPUT/`.
4. Action **Write File** nutzen (Append = Off), vollständigen Inhalt schreiben.
5. Optional danach den Dateipfad ins Clipboard schreiben.

## Hinweise
- Keine künstliche Größenbegrenzung unter 100 MB.
- Kein stilles Abschneiden von Text.
- Für große Artefakte Datei-Modus nutzen.
