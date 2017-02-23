FROM maven:alpine

ENV redist_host redis
ENV JAVA_OPTS -Xms128m -Xmx512m

RUN apk add --update git && \
    rm -rf /var/cache/apk/*

RUN git clone https://github.com/SchweizerischeBundesbahnen/compass.git && \
    cd compass && \
    mvn clean install && \
    cp /root/.m2/repository/ch/sbb/compass/*/compass-*.jar /compass.jar && \
    rm -rf /compass

EXPOSE 8080

CMD java -jar /compass.jar -Dredis.host=${redis_host} ${JAVA_OPTS} 
