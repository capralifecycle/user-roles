FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:65182054459de6205fd7709eb7fa28337ee9c9a124b9ebd6765069d86511c1bb

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
