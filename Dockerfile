FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:7a601a23a5af713640c82057f5be0c3b9693021694e4e3624415259e87a3f5a1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
