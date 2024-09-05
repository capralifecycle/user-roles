FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:67263db32abfea27753fb133fc9b40a5b2bc6931097c379caa17d98ebc034ee2

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
