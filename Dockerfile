FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:f830186a4b4d09ba46ac799077706933cfa8d03898eb606a013f2b0afee3bcfc

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
