FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:332a9bc69b2672ff42c9d04876258a0b4e7a7a5c6c05302baef0d7aa27e1f765

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
