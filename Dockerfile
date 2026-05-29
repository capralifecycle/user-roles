FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:e74a3eb6fbb6e4e3cafcedfa8d2588735610acb5df84d475af6b9980405f3e2e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
