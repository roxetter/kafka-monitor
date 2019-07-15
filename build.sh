#! /bin/bash

deploy_dir="deploy/kafka-monitor"

# cleanup
rm -f ${deploy_dir}/*.jar

mvn -f pom.xml -Dmaven.test.skip=true clean install
if [ $? -eq 0 ]; then
    mv target/*.jar ${deploy_dir}
    cp readme.md ${deploy_dir}
fi
rm -rf target
