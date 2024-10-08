FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:220a56cfb5aeaa31ab15d818d2322108d449ca49e859a42f5c53f764cf1dc9d5

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
