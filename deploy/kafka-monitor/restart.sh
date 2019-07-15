#!/bin/sh

export LANG="zh_CN.UTF-8"
ps -ef | grep kafka-monitor | grep -v grep | awk '{print $2}' | xargs kill > /dev/null 2>&1
if [[ $? -eq 0 ]]; then
    echo "Stopping, please wait ..."
    sleep 10
fi

nohup java -jar kafka-monitor-0.0.1-SNAPSHOT.jar > /dev/null 2> error.log &