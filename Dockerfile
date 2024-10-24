FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:af4df00adaec356d092651af50d9e80fd179f96722d267e79acb564aede10fda

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
