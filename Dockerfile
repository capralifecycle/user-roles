FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:54b46d41e20e3472b95feb3c0bb68d616d97d64d276f298cb433c28209a7bd84

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
