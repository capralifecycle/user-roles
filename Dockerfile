FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:b10a2152416e1326abebc5a21c5e71a1e4caf12878a64e205b0dca8de6c3e4f5

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
