FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:5fd4ba1fd71029f6f36720a1a9ac5fe078bc7e80aec6e0f47b4cacc9959053d7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
