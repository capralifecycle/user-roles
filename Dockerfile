FROM azul/zulu-openjdk-alpine:19-jre@sha256:6cb209c7562060b2d3f44ca71ef636d27806d8b96f487fa318e0dace5fee913d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
