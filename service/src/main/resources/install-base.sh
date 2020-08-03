NAME=$1
JAR=$2
USER=$3
START_SCRIPT=$4

#Logic

LOCAL_SERVICE_FILE=service
SERVICE_FILE=/etc/systemd/system/$NAME.service
BASE_DIR=/home/$USER/services/$NAME
BIN_DIR=$BASE_DIR/bin
LOG_DIR=$BASE_DIR/log
JAR_PATH=$BIN_DIR/$NAME.jar

echo Stopping service...
service $NAME stop || echo Service was not running

# Setup directories
mkdir -p $BIN_DIR
mkdir -p $LOG_DIR
sudo chown -R $USER $BASE_DIR

# Copy JAR and dependencies
cp target/$JAR $JAR_PATH
cp target/lib/* $BIN_DIR
cp $START_SCRIPT $BIN_DIR/
echo Copied jar file to $JAR_PATH

# Install service file
cp $LOCAL_SERVICE_FILE $SERVICE_FILE
echo Copied service file to $SERVICE_FILE
systemctl daemon-reload

echo Installed

service $NAME restart
service $NAME status
