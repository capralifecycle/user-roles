FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:a42f46fd6863030e6f859793cd6a19c538dbc770c979eb7164e77575dc7b06db

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
