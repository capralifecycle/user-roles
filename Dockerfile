FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:2e8c1c8e62593f5e348d7fcf027729de4f03a8230befce3de8f2cc6d22c642ab

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
