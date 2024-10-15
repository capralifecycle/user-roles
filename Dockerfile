FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:12e335ed6d5cb4cd55ef6b5e2e769b2a77f25ced0270df1277bc2bca3dfd209e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
