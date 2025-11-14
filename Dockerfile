FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:39028d7bc400c64c27261655edeaddeea2c63aeddb2f65e1c1ccd85b5e90c0a2

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
