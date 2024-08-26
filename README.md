# FileSearchApp

## Beschreibung
Die **FileSearchApp** ist eine Java-Anwendung zur Dateisuche mit folgenden Funktionen:
- **Dateiname-Suche**: Findet Dateien in einem ausgewählten Verzeichnis und dessen Unterverzeichnissen, die einem angegebenen Dateinamen-Muster entsprechen.
- **Dateiinhalt-Suche**: Durchsucht den Inhalt der gefundenen Dateien nach einem bestimmten Suchbegriff, optional unter Berücksichtigung der Groß- und Kleinschreibung.
- **Ergebnisse anzeigen**: Zeigt die gefundenen Dateien in einer Tabelle an, einschließlich Dateipfad und Größe. Erlaubt das Öffnen der Dateien oder das Anzeigen ihrer Position im Dateiexplorer per Rechtsklick.
- **Benutzeroberfläche**: Ermöglicht die Eingabe von Suchkriterien, die Auswahl eines Verzeichnisses und die Ausführung der Suche durch einen Button.

## So starten Sie das Programm

### Unter Windows
Erstellen Sie eine Batch-Datei `start_file_search.bat` mit folgendem Inhalt:

batch
@echo off
REM Startet die FileSearchApp Java-Anwendung
REM Autor: ChatGPT (OpenAI)
java -jar path\to\your\FileSearchApp.jar
pause


### Unter Linux
Erstellen Sie ein Bash-Skript start_file_search.sh mit folgendem Inhalt:

#!/bin/bash
# Startet die FileSearchApp Java-Anwendung
# Autor: ChatGPT (OpenAI)
java -jar /path/to/your/FileSearchApp.jar


Lizenz
Das Programm und die bereitgestellten Anweisungen sind frei verfügbar.
Hinweis: Die Anweisungen und das Programm wurden von ChatGPT (OpenAI) erstellt.
