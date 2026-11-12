# Fortnite Kistentracker für Android

Manueller Zähler für Fortnite Mobile. Kleine, verschiebbare Anzeige mit **+1** und **−1** über dem Spiel. Die App speichert die Kisten dieser Runde und die Gesamtzahl lokal.

## APK bauen

Dieser GitHub-Workflow baut bei jedem Push auf `main` eine Debug-APK.

1. Öffne den Tab **Actions**.
2. Wähle **Build Fortnite Kistentracker APK**.
3. Öffne den neuesten erfolgreichen Lauf.
4. Lade unten das Artifact **Fortnite-Kistentracker-APK** herunter.
5. ZIP entpacken und `app-debug.apk` auf dem Android-Handy installieren.

Beim ersten Start musst du die Android-Berechtigung **Über anderen Apps einblenden** zulassen. Drücke dann **Overlay starten**. Ziehen am Griff **⋮⋮** verschiebt den Zähler; **+** zählt eine Kiste und **−** macht die letzte Rundenzählung rückgängig.

Die App zählt **manuell**, nicht automatisch. Fortnite wird nicht verändert. Ob das Overlay sichtbar ist, kann je nach Gerät und Spielmodus variieren.

## Datenschutz

Die App speichert nur die Zähler lokal auf dem Gerät. Sie benötigt keine Bildschirmaufnahme, keine Internet-Berechtigung und keinen Fortnite-Account.
