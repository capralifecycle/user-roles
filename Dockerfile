FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:26526ac37526bd810ed372f8bb0fb4f663cb5ebe9d4cd9e2b2d715ea901fb807

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
