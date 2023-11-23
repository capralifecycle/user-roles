FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:59bd38710f83a514b09cadef783f2877382b3d7bc800a9d219a0fcff15c60273

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
