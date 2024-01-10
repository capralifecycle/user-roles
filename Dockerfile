FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:10abd6b4f7eb2d7dbf11cd1529d40ca6e8d396695d7a0092e7970a258a47a48d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
