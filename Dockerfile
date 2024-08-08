FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:5f5be32277034a6cccfdb6eaa17d2bf0d1272d662f2e09c38888c3989c1c89ac

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
