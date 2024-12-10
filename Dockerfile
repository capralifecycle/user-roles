FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:1cecd102f85abd32c2a0b309748552e54f347346e2c59015027f2dc847c44bc1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
