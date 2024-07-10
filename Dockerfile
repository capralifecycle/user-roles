FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:69131f6ada8b34218288f2a8222670e0aad54c8e67c403f573cf267e286feefc

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
