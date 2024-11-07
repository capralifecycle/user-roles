FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:eebeced02fbc651c939e966fbff0cf14fcd4b0fbbb95bbfde15f062e9fdb60e4

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
