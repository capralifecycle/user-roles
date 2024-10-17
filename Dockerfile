FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:712810c62f447aae67b0a08812b0d9baf3265f5fb1d7c6f4ec4a290637e9ab34

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
