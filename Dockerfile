FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:e99d11efb05102d388531f04bffa03d42ddcf1604b2b9cbf0ba2bda415da29cf

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
