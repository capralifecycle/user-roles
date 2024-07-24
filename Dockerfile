FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:02b7e1e0dac97814067b36b17ac594996fa4541590f6f5b438f8883548cb1f35

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
