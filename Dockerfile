FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:3543b2a926e93d048321bcaa266f0a3702fa42cc85999691772a6250596ec474

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
