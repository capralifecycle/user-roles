FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:e13356c443782f86e39199ee69b0f4709930d329bbe4c545764cea63603dd2f4

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
