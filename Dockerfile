FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:81ea15ee232b961ce6217485814d694f969f2a0a344f9023e62967115c963bd9

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
