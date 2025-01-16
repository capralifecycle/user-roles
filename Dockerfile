FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:53db89886ca6391c54face55d5b7b95e65e532dd00f2d340d20bb30abaf1e667

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
