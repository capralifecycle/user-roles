FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:f634ae2156d16ddb564d41cf645dfa6cf66c7970643f8f9b5de0c345c9a3f8bf

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
