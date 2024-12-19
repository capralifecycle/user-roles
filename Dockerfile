FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:2b8a989e2c35893bc084c847773690086b5a015c7d126ef40f91769748a047bb

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
