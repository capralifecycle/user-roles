FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:72bd657a22aca9b945476bb62359dfab9be066006646387d5811d1a570a17837

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
