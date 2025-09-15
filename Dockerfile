FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:6597806eb246b7e43237ef09f63c3de73e73c53c8e174c8187c92bbc3c0810b3

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
