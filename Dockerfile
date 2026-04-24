FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:a940cc5e371ae25279675eba541e5af8f84b22656c2534d229b3bd4c316e0534

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
