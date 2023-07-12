FROM azul/zulu-openjdk-alpine:20-jre@sha256:8fe60352d10cc66013f0e25e49a66c513f87e89c035f04d4bbcfb41ca59ac649

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
