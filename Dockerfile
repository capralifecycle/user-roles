FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:b83587882abeee0de13fa15b5c27863a9d80b72db1c2bf0689d675e3670de287

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
