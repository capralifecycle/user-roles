FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:5ce0ca2562586fe548fa4147ef4badfab37fdbbb9620a1b8573b96e2ae7de8ac

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
