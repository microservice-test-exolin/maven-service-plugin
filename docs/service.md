# Service
Ein Service ist ein Hintergrundprozess welcher automatisch gestartet wird.

Schnittstellen:
* HTTP-Port (optional), welche auf eine Domain in nginx gemappt werden kann => Environment variable `PORT`
* Datenbank (optional): Die Konfiguration bekommt der Service bereitgestellt => Environment variable `DB_CFG_FILE` mit Pfad zu Properties-File mit den Keys `url`, `username` und `password`
* Service specific ones like bot tokens
