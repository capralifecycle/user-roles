FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:98efb8ecb085729b96f253edf29c35d2ec674ecf9a0449a5bcc886af5c667d5d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
