FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:90849e129bc0e3f183c18091a3fdfad0769bf5c124accca688a6ca1b9db01bd4

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
