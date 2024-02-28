FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:00b17b782cac19ae74a7f5b62414849cc1c674c8d440c34f3f0ac4ec86113123

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
