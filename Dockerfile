FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:a23da68af4e74bd84cd3f3650d74c69a277d285a43ba19588608aa352df6edf3

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
