# Filesharing Server

- Abdou Arsani (ic20b098@technikum-wien.at)
- Parzer Florian (ic20b081@technikum-wien.at)

## Must-Have Features

- [x] JavaFX Client als GUI
- [x] Basic Konfiguration mittels Config-File
- [x] File upload/download
- [x] Löschen von Dateien
- [x] Dateien umbenennen
- [x] Anzeigen der Files mittels Ordnerstruktur
- [x] Zugriff gleichzeitig über mehrere Clients möglich

## Should-Have Features

- [x] Upload der Files mittels Drag&Drop (Arsani)
- [x] Markieren bestimmter Files als Favorieten (Arsani)
- [x] Eigene Threads für den Upload und Download im Hintergrund (Florian)

## Nice-to-have Features

- [ ] Usergruppen fürs Anzeigen von Dateien
- [ ] Admingruppe fürs Bearbeiten der Usergruppen und der Rechte

## Overkill

- [ ] Verschlüsselung der Kommunikation zum Server


## Zusammenfassung unseres Projekts
Unser Projekt (Share_IT) leistet die Funktionalität eines Filesharing Servers. Bei Start des Programms erscheint ein kleines Log in Fenster, bei dem IP Adresse und Prot (mit ":" getrennt) eingegeben werden müssen. Nach erfolgreichem Log in erkennt man schon die graphische Benutzeroberfläche des Users. Files können entweder mittels Pfadauswahl und Betätigung des Upload Buttons oder durch einfaches Drag and Drop auf den Server hochgeladen werden. Die hochgeladenen Dateien werden durch eine Ordnerstruktur auf der graphischen Benutzeroberfläche angezeigt. Einen neuen Ordner erstellt man durch das klicken auf "Create Directory". Diese werden dann mit blau hervorgehobener Schrift angezeigt. Um in einen Ordner hineinzusehen klickt man auf "Enter", um wieder zurück in den Parentordner zu kommen gibt es oben links einen Return Button. Der aktuelle Pfad in der Ordnerstruktur wird über der Ordnerstruktur angezeigt. Außerdem gibt es einen Refresh Button, der bei gleichzeitiger Nutzung von mehreren Usern behilflich sein kann. Sowohl Files als auch Directories können umbenannt, verschoben und gelöscht werden. Files kann man außerdem noch als Favorit markieren, dann erscheint neben dem File ein Stern Symbol. Das Herunterladen von Dateien erfolgt durch einen Klick auf den Download Button. Danach wird man nach einem lokalen Zielordner gefragt wo der Download anschließend erfolgt. Der unterste Button ist dient dem Beenden des Programms.

![Alt text](/GUI.PNG?raw=true "Ansicht nach Login")
