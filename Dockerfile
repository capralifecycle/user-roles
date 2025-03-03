FROM azul/zulu-openjdk-alpine:21@sha256:1b04c66f9a2e830570081b43a3bb12f460bd7ce77c5a328b9054c75fbfb4c049

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
