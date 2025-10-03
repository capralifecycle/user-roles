FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:7caf4a1a0e4cad115d2ddbbb70bfa526c18ffb03cca27cb26070d528d6ef846f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
