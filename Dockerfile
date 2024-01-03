FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:74b33baa82fb5e5ca90ec38c5395f0e852e60425023be6cdd9e86768ae9ec4f1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
