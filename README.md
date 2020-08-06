# maven-service-plugin
<!-- markdown-link-check-disable -->
![Java CI with Maven](https://github.com/microservice-test-exolin/ubuntu-services/workflows/Java%20CI%20with%20Maven/badge.svg)
![Check Markdown links](https://github.com/microservice-test-exolin/ubuntu-services/workflows/Check%20Markdown%20links/badge.svg)
<!-- markdown-link-check-enable -->

## [maven deployment plugin](service)
* deployment (goal `deploy`):
  * setup directory structure 
  * copy binaries 
  * creating service file
  * create start script

## service management web app
Itself a service, providing a web interface for controlling services:
* start, stop, restart
* status report: if running, in case of error the reason
* log view
* update:
  * git pull
  * mvn package
  * mvn org.exolin.msp:maven-service-plugin:1.0-SNAPSHOT:deploy
