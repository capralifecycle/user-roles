FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:0531ceddd9704131df31cd8014504cf9fe9cb01f3cc8af8ae90991d70e8bb2f7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
