#!/bin/sh

APP_HOME=$(dirname $(cd "$(dirname "$0")"; pwd))

#公用的验证服务，现阶段有两个服务,appauth-service和devauth-service
APP_NAME=appauth-service

. /etc/init.d/functions
. /etc/profile

_jar=`ls ${APP_HOME}/lib/*.jar`
_classpath=`echo ${_jar} | sed -e 's/ /:/g'`

CLASSPATH=$APP_HOME/conf:${_classpath}

#port=7091 对应 appauth  port=7092 对应 devauth
JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Xms512m -Xmx512m -Xmanagement:ssl=false,authenticate=false,autodiscovery=false,port=7091"

APP_OPTS="-l $2"

MAIN_CLASS="com.babeeta.butterfly.auth.AuthenticationServer"

PID=/var/run/${APP_NAME}

touch $PID

LOG=/var/log/${APP_NAME}

JAVA=$JAVA_HOME/bin/java

if test -z $1 ; then
	echo 'Usage service.sh [start|debug|stop] listen_address mongodbHost:mongodbPort'
	exit 1
else
	if ! test -x $JAVA ; then
		failure "Cannot find jvm or jvm is not executable";
		exit 1;
	fi

	if ! test -d $LOG ; then
		mkdir -p $LOG;
	fi

	if ! test -d $LOG ; then
		failure "Cannot create directory for log:$LOG";
		exit 1;
	fi

	if ! test -e $PID ; then
		failure "Cannot create pid file:$PID";
		exit 1;
	fi

	case $1 in
		'start' )
			if ! test -z `cat $PID` ; then
				failure 
				echo 'Router Server is still running. Its pid is '`cat $PID`
				exit 1;
			else
				exec $JAVA_HOME/bin/java $JAVA_OPTS  -classpath $CLASSPATH $MAIN_CLASS $APP_OPTS>> $LOG/std.out 2>&1 &
				echo $! > $PID
				success 
				echo "Starting Router Server on $2....."
				exit 0;
			fi
		;;
		'debug' )
			if test -z $4 ; then
				failure
				echo "Usage service.sh [start|debug|stop] domain listen_address mongodbHost:mongodbPort debugPort"
				exit 1;
			fi

			if ! test -z `cat $PID` ; then
				failure
				echo 'Router Server is still running. Its pid is '`cat $PID`
				exit 1;
			else
				JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=$4,server=y,suspend=y"
				exec $JAVA_HOME/bin/java $JAVA_OPTS  -classpath $CLASSPATH $MAIN_CLASS $APP_OPTS>> $LOG/std.out 2>&1 &
				echo $! > $PID
				success 
				echo 'Starting Tunnel Server in debug mode......'
				exit 0;
			fi
		;;
		'stop' )
			if ! test -z `cat $PID` ; then
				kill `cat $PID`
				echo "" > $PID
				success "OK"
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
