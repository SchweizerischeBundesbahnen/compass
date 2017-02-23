FROM maven:alpine

ENV branch ${SOURCE_BRANCH}

ONBUILD git clone https://github.com/SchweizerischeBundesbahnen/compass.git

ONBUILD cd compass && git checkout -b ${SOURCE_BRANCH} && mvn clean install

EXPOSE 8080

CMD java -jar ${ARTIFACT_ID}-${ARTIFACT_VERSION}.jar -Dredis.host=${REDIS_HOST} -Xms128m -Xmx512m
