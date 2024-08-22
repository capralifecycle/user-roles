FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:ffec4ce8ddafa12954b654714a10f361a14fe167a97c34d3cd9dc5f2632ec70c

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
