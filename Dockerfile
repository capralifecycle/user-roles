FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:e932d8cae42f2e0c79ecd96277a16b6cee20d70023e5f312f6fd73400785371b

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
