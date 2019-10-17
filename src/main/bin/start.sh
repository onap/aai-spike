#!/bin/sh

APP_HOME="/opt/app/spike"

if [ -z "$CONFIG_HOME" ]; then
    echo "CONFIG_HOME must be set in order to start up process"
    exit 1
fi

if [ -z "$SERVICE_BEANS" ]; then
    echo "SERVICE_BEANS must be set in order to start up process"
    exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
    echo "KEY_STORE_PASSWORD must be set in order to start up process"
    exit 1
fi

PROPS="-DAPP_HOME=$APP_HOME"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
PROPS="$PROPS -Dlogging.config=$APP_HOME/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD"
JVM_MAX_HEAP=${MAX_HEAP:-1024}

# Changes related to:AAI-2181
# Change aai spike container processes to run as non-root on the host
USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}
SPK_LOGS=/var/log/onap/AAI-SPK

if [ $(cat /etc/passwd | grep aaiadmin | wc -l) -eq 0 ]; then

        groupadd aaiadmin -g ${GROUP_ID} || {
                echo "Unable to create the group id for ${GROUP_ID}";
                exit 1;
        }
        useradd --shell=/bin/bash -u ${USER_ID} -g ${GROUP_ID} -o -c "" -m aaiadmin || {
                echo "Unable to create the user id for ${USER_ID}";
                exit 1;
        }
fi;

chown -R aaiadmin:aaiadmin ${MICRO_HOME}
chown -R aaiadmin:aaiadmin ${APP_HOME}
chown -R aaiadmin:aaiadmin ${SPK_LOGS}

find ${MICRO_HOME}  -name "*.sh" -exec chmod +x {} +

gosu aaiadmin ln -s /logs $MICRO_HOME/logs
JAVA_CMD="exec gosu aaiadmin java";


set -x
${JAVA_CMD} -Xmx${JVM_MAX_HEAP}m $PROPS -jar ${APP_HOME}/spike.jar
