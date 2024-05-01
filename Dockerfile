FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:1ba9606439dc2eebc3a7f37bbbc49be98ed167d57f498ac603e2267bdcaab8e9

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
