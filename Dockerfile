FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:a31131cca7e34fceefb578a1c26e568caa2f8619deb5ce612c54afcb2def52e2

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
