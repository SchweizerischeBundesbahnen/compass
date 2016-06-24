FROM java

ENV ARTIFACT_GROUPID ch.sbb
ENV REPO_ID hosted.mwe-wzu.snapshots
ENV ARTIFACT_ID compass
ENV ARTIFACT_VERSION LATEST
ENV ARTIFACT_URL http://repo.sbb.ch/service/local/artifact/maven/content?r=${REPO_ID}&g=${ARTIFACT_GROUPID}&a=${ARTIFACT_ID}&v=LATEST

RUN curl $ARTIFACT_URL -o ${ARTIFACT_ID}-${ARTIFACT_VERSION}.jar

EXPOSE 8080

CMD java -jar ${ARTIFACT_ID}-${ARTIFACT_VERSION}.jar