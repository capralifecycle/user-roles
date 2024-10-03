FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:c15f40ec86a7f97849f1b121e80ebcad5121265ab2d93dba5853f0b2df150653

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
