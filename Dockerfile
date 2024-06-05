FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:5ba01ac30fcc3d7be766ad55dae06de90b15fe0d940baac94b23ddb231e8b089

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
