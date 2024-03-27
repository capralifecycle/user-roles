FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:481e456e85fe8b6fa2d6bd9c3c268abc53f414db5030b61b648184a5670d868e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
