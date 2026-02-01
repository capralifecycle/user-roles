FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:ed9003dc0b6926c261d5e0a7d22ae89afaad7c4c70f6d1c1e38d6dc06c50932f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
