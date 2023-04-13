FROM azul/zulu-openjdk-alpine:19-jre@sha256:ad4d2dcf7df4205ae19aa596b7162969ee7c4a0e81658dec0ad71dbbc2c895cf

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
