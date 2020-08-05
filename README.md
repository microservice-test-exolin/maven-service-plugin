# maven-service-plugin
<!-- markdown-link-check-disable -->
![Java CI with Maven](https://github.com/microservice-test-exolin/ubuntu-services/workflows/Java%20CI%20with%20Maven/badge.svg)
![Check Markdown links](https://github.com/microservice-test-exolin/ubuntu-services/workflows/Check%20Markdown%20links/badge.svg)
<!-- markdown-link-check-enable -->
Research to get automated Ubuntu services

***Possibilities:***

**Docker**
* Isolation: yes
* Rating: Requires to be learned first

**Tomcat**
* Isolation: no
* Rating: Deploy ain't easy, especially on errors

**[systemd service](systemd-service.md)**

systemd service running `java -jar ...`
* Isolation: yes
* Rating: unless tool is found every new service requires adding a new configuration file or even script

**custom service manager**
* Isolation: yes
* Rating: Requires reinventing the wheel and supporting on my own which makes it bug prone
