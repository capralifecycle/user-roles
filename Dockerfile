FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:fd59e6c29c6c3ecfa22fc277d9e88f67229376ecdf695a316c595f5f1c6a2ae1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
