FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:9704974c68236ff9b466a4ad20fe4f5332f0fc469bffaeccafc6a8468bda9640

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
