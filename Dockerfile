FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:16e0ff8d09d430242e1a2be613175e8b3276039cac56912a868cdb03d118f988

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
