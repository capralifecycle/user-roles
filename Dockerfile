FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:110e247161dc7d1061340e3246a6d7820bbbf6ce8ff588c3b6753eff46f97a0e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
