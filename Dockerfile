FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:2af0e3fadd824ee78c4f805a8dd09c470032940ede9dd36a1a33f079536de403

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
