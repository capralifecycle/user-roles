FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:f444fca5f56773698f17acded45bc0698e92e8fcd7b42d5310650499572f011d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
