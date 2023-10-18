FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:21297049d215447d157a80c50b7cab91bd958b26e0e1bcad0eb7c9a327577987

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
