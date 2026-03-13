FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:c55ebcd68eccb75dff84269695a74e01a435b1eefc307caf56dc81470fec2f00

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
