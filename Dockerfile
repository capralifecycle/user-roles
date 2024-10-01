FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:6dcb0563ec4171acad46bc8775daae4dcf8902feaccfa3d9d64d47b14043419c

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
