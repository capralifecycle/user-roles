FROM azul/zulu-openjdk-alpine:19-jre@sha256:50f498af87b67c7638d025d0b6d3545f6ea5b6723dc3278a64cafa45ee9d7b23

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
