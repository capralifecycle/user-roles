FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:1b76965bc244c484dc150b2ac0fa326cf7255f3d4bbad379bced56d3673b0309

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
