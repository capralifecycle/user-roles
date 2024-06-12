FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:a0f81e81494d7b2181d28a9f00aa4604b44c5cda9f0c89e6f023b06a57cf5a9f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
