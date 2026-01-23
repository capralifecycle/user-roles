FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:c1ccb8a1912bbff939a99c02297782d6c7d00a5bb8598a9e1134cdd12cf3348d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
