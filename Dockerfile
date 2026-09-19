FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:b25a57b3de4609cb559222e46dc26382675ae26b6c66cdb7dfa13d6bc7d4570e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
