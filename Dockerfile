FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:528cbafe8bea9f600b7a3ec27e76b06549b274cd38ba36e84c74f46eeeefb7a4

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
