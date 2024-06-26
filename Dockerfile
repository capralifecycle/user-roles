FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:e465a92c4c9ce43b930c04a3db8073ac7edd63462f6ac8fe121ee349e53a2540

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
