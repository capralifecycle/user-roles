FROM azul/zulu-openjdk-alpine:20-jre@sha256:98935e8a93fbde4ef3481b1c9a67bb288f7e62a4b43c61b8e4f22dc0fe3546f1

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
