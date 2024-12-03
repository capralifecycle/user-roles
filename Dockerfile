FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:953f32c010273d3b0fa0ad2ede14d2f0a48a48ca7f1dbd740ba6651d4243e3a5

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
