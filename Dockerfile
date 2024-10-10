FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:917e6da5379097bf7baaef6129f009353014acf024f89f85d86f4a609f4a8ce7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
