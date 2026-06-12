FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:8a4fa52c9d2652a646004d787aae43539b5c9ea7137fa48e20c7399da2c84a07

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
