FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:587ec5fbbeb12f35a3048d88c73f805c20678077fdf961ecc5801a82e6fffed2

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
