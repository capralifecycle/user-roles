FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:e9a5b5a02cc69567630935f8dc35b616dded9431b56c4c14c953a2541273f111

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
