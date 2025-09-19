FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:5e65f3948665b742b5c8716799fef2c354f5d4566890c9452de010816239d025

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
