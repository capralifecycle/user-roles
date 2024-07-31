FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:9e965fd686929412b15a86c60ba9d82810b02b624cbb3d00afab8f1918e0449a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
