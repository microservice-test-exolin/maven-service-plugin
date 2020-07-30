# Systemd service

Turning it into a service
Let’s create a file called /etc/systemd/system/rot13.service
```ini
[Unit]
Description=ROT13 demo service
After=network.target
StartLimitIntervalSec=0
[Service]
Type=simple
Restart=always
RestartSec=1
User=centos
ExecStart=/usr/bin/env php /path/to/server.php

[Install]
WantedBy=multi-user.target
```

```ini
[Unit]
Description=My PHP Daemon Service
#May your script needs MySQL or other services to run, eg. MySQL Memcached
Requires=mysqld.service memcached.service 
After=mysqld.service memcached.service

[Service]
User=root
Type=simple
TimeoutSec=0
PIDFile=/var/run/myphpdaemon.pid
ExecStart=/usr/bin/php -f /srv/www/myphpdaemon.php arg1 arg2> /dev/null 2>/dev/null
#ExecStop=/bin/kill -HUP $MAINPID #It's the default you can change whats happens on stop command
#ExecReload=/bin/kill -HUP $MAINPID
KillMode=process

Restart=on-failure
RestartSec=42s

StandardOutput=null #If you don't want to make toms of logs you can set it null if you sent a file or some other options it will send all php output to this one.
StandardError=/var/log/myphpdaemon.log
[Install]
WantedBy=default.target
```

`ExecStart` ist der Service

## Starting in the right order
You may have wondered what the After= directive did. It simply means that your service must be started after the network is ready. If your program expects the MySQL server to be up and running, you should add:
After=mysqld.service

## Restarting on exit
By default, systemd does not restart your service if the program exits for whatever reason. This is usually not what you want for a service that must be always available, so we’re instructing it to always restart on exit:
Restart=always
You could also use on-failure to only restart if the exit status is not 0.
By default, systemd attempts a restart after 100ms. You can specify the number of seconds to wait before attempting a restart, using:
RestartSec=1
## Avoiding the trap: the start limit
I personally fell into this one more than once. By default, when you configure Restart=always as we did, systemd gives up restarting your service if it fails to start more than 5 times within a 10 seconds interval. Forever.
There are two [Unit] configuration options responsible for this:
StartLimitBurst=5
StartLimitIntervalSec=10
The RestartSec directive also has an impact on the outcome: if you set it to restart after 3 seconds, then you can never reach 5 failed retries within 10 seconds.
The simple fix that always works is to set StartLimitIntervalSec=0. This way, systemd will attempt to restart your service forever.
It’s a good idea to set RestartSec to at least 1 second though, to avoid putting too much stress on your server when things start going wrong.
As an alternative, you can leave the default settings, and ask systemd to restart your server if the start limit is reached, using StartLimitAction=reboot.
