#!/bin/sh
SERVICE_NAME=avs
PATH_TO_JAR=/usr/local/ausregistry/avs/avs_v2-1.0.0-standalone.jar
PID_PATH_NAME=/tmp/avs-pid
case $1 in
        start)
                echo "Starting $SERVICE_NAME ..."
                if [ ! -f $PID_PATH_NAME ]; then
                        nohup java -jar $PATH_TO_JAR 2>> /var/log/avs/avs.err >> /var/log/avs/avs.std &
                        echo $! > $PID_PATH_NAME
                        echo "$SERVICE_NAME started ..."
                else
                        echo "$SERVICE_NAME is already running according to $PID_PATH_NAME -  removed file to start if avs is ded"
                fi
        ;;
        stop)
                if [ -f $PID_PATH_NAME ]; then
                        PID=$(cat $PID_PATH_NAME);
                        echo "$SERVICE_NAME stoping ..."
                        kill $PID;
                        echo "$SERVICE_NAME stopped ..."
                        rm $PID_PATH_NAME
                else
                        echo "$SERVICE_NAME is not running ..."
                fi
        ;;
        restart)
                if [ -f $PID_PATH_NAME ]; then
                        PID=$(cat $PID_PATH_NAME);
                        echo "$SERVICE_NAME stopping ...";
                        kill $PID;
                        echo "$SERVICE_NAME stopped ...";
                        rm $PID_PATH_NAME
                        echo "$SERVICE_NAME starting ..."
                        nohup java -jar $PATH_TO_JAR /tmp 2>> /dev/null >> /dev/null &
                        echo $! > $PID_PATH_NAME
                        echo "$SERVICE_NAME started ..."
                else
                        echo "$SERVICE_NAME is not running ..."
                fi
        ;;
esac
