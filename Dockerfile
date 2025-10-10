FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:9f7259b19776a7303f19ab36f5adead1b0d88b7b58bc7f0b0b295abef1b553aa

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
