FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:011a4ad251e615c8c32fd2a7dbc42888aa91b17647d5b529a9db8dc0d66f695f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
