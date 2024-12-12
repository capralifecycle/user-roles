FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:e68c77f6490761ed0c626b60c269053564af84bb3fec8d9d746b0c658f9f57b4

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
