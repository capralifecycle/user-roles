FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:9caba4d283af97b6a24d34164e5ec8a0571a14ce5f179fbe60df7803f899f05a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
