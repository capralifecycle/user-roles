FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:3dd53ed02013cb7caa41f5b4d516ebbb1c92561fd1deb5638357154bb9860071

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
