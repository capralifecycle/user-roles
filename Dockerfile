FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:74333c91821603ad49251f36a8aefd683625cc4d9650555766dd497d7f9f2344

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
