FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:4cf847a9595965f47982f96f92c93cd31eb802825835e26ed4692187d3b28f1d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
