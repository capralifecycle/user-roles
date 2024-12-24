FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:437ed2ce81d687417cc1ce4a2fd28b4d6ef3f42b1b89588a5f22283976e22652

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
