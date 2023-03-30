FROM azul/zulu-openjdk-alpine:19-jre@sha256:e5159a9058387e4c6b3bebc7c65625382fc2465bcb442b54b8e811d461bb9a79

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
