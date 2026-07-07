FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:b2a82f36a4b969681f98fbd59432d776dee1f533983d3e365dc0ecb8f8e2d5c7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
