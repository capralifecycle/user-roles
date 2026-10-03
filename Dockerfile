FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:a86bf89b307784b8ef9171cfdc2ea9959e93e7837b63f05f0b2dc1a3521a9505

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
