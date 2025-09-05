FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:31d4f6f75f3bf5fabe712afd2c203ec70aa5c9d0755ff9cf7e7bd78a7bbca58a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
