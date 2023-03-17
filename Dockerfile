FROM azul/zulu-openjdk-alpine:19-jre@sha256:6f1aa679b6854e6f82afac6a69a3414ce689a2e991f0ddd90fbdee5c56eb0637

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
