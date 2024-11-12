FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:373ceefd3503e055131d34cd0143c943468b1d09a09df3fcd2a008edface6c70

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
