FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:f652be8ddd97b0e8cd1a7662a885ade4cf3e02b8b299ca60acfda3d95bcb40ba

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
