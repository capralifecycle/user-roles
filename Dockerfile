FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:ea64a6682b55534f164a3e2c6478259295caf19eae075df8301692852db676a1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
