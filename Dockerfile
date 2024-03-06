FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:6883867f848d89fed19b730813e55ea8575c814728273f3e0ab0bf3564de2f6f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
