FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:d25045ccce399c0a09c79bd0268e82fa96b9792518ff68d9873984858956301e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
