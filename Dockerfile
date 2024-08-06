FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:127af4402dba14580df13f79e0c921cf2e6139fc2e8432439b2e1a2a9820756b

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
