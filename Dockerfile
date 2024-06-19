FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:ed11af3adbe616a65541bc4513313ce398371562fc436ac972aac4a8ce658443

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
