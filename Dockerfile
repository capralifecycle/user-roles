FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:80204cd019694c151cfa210da05d9ee7cc90d82d925fd8345e5fc69192400d44

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
