FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:b2d3090f9ddf43d4b11431040a1220bcc196f6b2c5e7dc438a6cd1add3929f2e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
