FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:b0a6df10aa6a1de97124ebf92c0dc9e03b706cdf7c480e6aede0ba73927aaf8a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
