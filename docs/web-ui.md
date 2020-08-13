## service management web app
Itself a service, providing a web interface for controlling services:
* start, stop, restart
* status report: if running, in case of error the reason
* log view
* [Build & Deploy](build+deploy.md)

Implementation
* using embedded web server:
  * configurable port
  * configurable if only accessible from localhost
