FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:741643d7fb403e89cf17c6a6f90e83cc61e4941ad4816d4536c89181a70324f7

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
