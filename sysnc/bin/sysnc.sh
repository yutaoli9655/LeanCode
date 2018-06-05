#!/bin/bash

PROJECT_DIR=$(dirname $0)/..
LIB_DIR=$PROJECT_DIR/lib
CONF_DIR=$PROJECT_DIR/conf
LOG_DIR=$PROJECT_DIR/log

if [ -f $PROJECT_DIR/bin/is_running ]
then
        echo "Process is running,exit..."
        exit 3
fi

touch $PROJECT_DIR/bin/is_running

for i in $(ls $LIB_DIR); do
	CLASSPATH=$LIB_DIR/$i:$CLASSPATH
done
CLASSPATH=$CLASSPATH:$CONF_DIR

java  -Xmx10240m -Dfile.encoding=UTF-8 -cp $CLASSPATH -Dlog.dir=$LOG_DIR MasterDataSyncApplication $*
[ -f $PROJECT_DIR/bin/is_running ] && rm -f $PROJECT_DIR/bin/is_running
