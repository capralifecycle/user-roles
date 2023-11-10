FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:b5ce43f06714c80d2c3b83eecdbc81125744f027085502129c7d1dd981edf6b9

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
