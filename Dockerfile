FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:dccc9b3348b68f4966824f85b47101ccf9f88ffee520ffb7fe30745e58d1432c

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
