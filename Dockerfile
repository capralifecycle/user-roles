FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:90ce6b02638d800cf3dc0b940aff47c2cce0f5de762ade849aefdca93e64d298

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
