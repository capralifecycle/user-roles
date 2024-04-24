FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:d1fd6024d97629d9d17dbec865b8b77d96cef8f973197addef0350f98d2da5dc

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
