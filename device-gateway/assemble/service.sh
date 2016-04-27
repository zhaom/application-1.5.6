#!/bin/sh


APP_HOME=$(dirname $(cd "$(dirname "$0")"; pwd))

APP_NAME=dev-gateway-service
. /etc/init.d/functions
. /etc/profile

_jar=`ls ${APP_HOME}/lib/*.jar`
_classpath=`echo ${_jar} | sed -e 's/ /:/g'`

CLASSPATH=$APP_HOME/conf:${_classpath}
LOG=/var/log/${APP_NAME}
PID=/var/run/${APP_NAME}
MAIN_CLASS="com.babeeta.butterfly.application.gateway.device.DeviceGatewayService"
JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Xms1024m -Xmx1024m -Xmanagement:ssl=false,authenticate=false,autodiscovery=false,port=7094"
APP_OPTS="-r $2"
RUNAS="devgateway"

mkdir -p $LOG


if test -z $1 ; then
	echo 'Usage service.sh [start|stop] device_gateway_number'
	exit 1
else
	case $1 in

		'start' )
			$APP_HOME/sbin/jsvc -procname $APP_NAME -pidfile $PID -user $RUNAS -outfile $LOG/std.log -errfile $LOG/err.log -cp $CLASSPATH $JAVA_OPTS $MAIN_CLASS $APP_OPTS
			success
			echo "Starting Router Server on $2....."
			exit 0;
		;;

		'stop' )
			if test -s $PID ; then
				kill `cat $PID`
				success
				echo 'Stopping Router Server......'
				exit 0;
			else
				failure
				echo "Router Server on $2 is not running. No need to stop it"
				exit 1;
			fi
		;;
	esac
	exit 0
fi
