FROM maven:alpine

ENV redist_host redis
ENV JAVA_OPTS -Xms128m -Xmx512m
ENV branch ${SOURCE_BRANCH}

RUN env

RUN apk add --update git && \
    rm -rf /var/cache/apk/*

RUN git clone https://github.com/SchweizerischeBundesbahnen/compass.git

RUN cd compass && git checkout -b ${SOURCE_BRANCH} && mvn clean install

EXPOSE 8080

CMD java -jar -Dredis.host=${redis_host} ${JAVA_OPTS} 
