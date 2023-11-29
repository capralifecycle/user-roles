FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:02750059e1c2a016e5507a6babd486ec3ded74e3f56b99398d9b3ab55d9f3ea7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
