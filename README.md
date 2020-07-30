# ubuntu-services
Research to get automated Ubuntu services

***Possibilities:***

**Docker**
* Isolation: yes
* Rating: Requires to be learned first

**Tomcat**
* Isolation: no
* Rating: Deploy ain't easy, especially on errors

**systemd service**

systemd service running `java -jar ...`
* Isolation: yes
* Rating: unless tool is found every new service requires adding a new configuration file or even script

**custom service manager**
* Isolation: yes
* Rating: Requires reinventing the wheel and supporting on my own which makes it bug prone
