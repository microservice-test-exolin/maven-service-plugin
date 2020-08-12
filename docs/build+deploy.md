# Build & Deploy
Build und deploy werdem im Kontext der [Web UI](web-ui.md) als [Prozesse](web-ui-process.md) ausgeführt

## Build
Das Kompilieren eines Standes

directory=git root
```bash
git pull
mvn package
```

## Deploy
Das Deployment als Service

directory=service project dir
```
mvn org.exolin.msp:maven-service-plugin:1.0-SNAPSHOT:deploy
```
