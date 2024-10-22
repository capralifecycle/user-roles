FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:04cef35be9296c1cf4ee9a7d7130a485d8c2f41380ed2efffaec36694a4367f5

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
