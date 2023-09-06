FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:f120d08b51ad87837ef94cd8f17c271e531429f2fba79b65f80221cd07289c9f

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
