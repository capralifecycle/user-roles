FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:a7b76070e70ae094ff844c41d6f86ef32606e4c6de13375e565d86b4ddfb058f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
