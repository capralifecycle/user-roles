FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:f813c3ca03ad2a169e1b4de1fc5a2ea16cb8a308ffa913ea1c757c27ee844e0b

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
