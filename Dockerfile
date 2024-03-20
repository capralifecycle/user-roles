FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:c4580a835aca618bfa82b614d1d5c771def44f302b8ce6d3f4750aa8fa968d3b

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
