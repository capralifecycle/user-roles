FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:6267762667a01b660a4cd5623b7902c5f44090a73f366878c4c01790e4bb9c46

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
