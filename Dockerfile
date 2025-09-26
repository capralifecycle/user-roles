FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:fa28fd282d7bbf1fa9cf5f21121254c7e6e340254ea3b833eb3d60ee922a293d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
