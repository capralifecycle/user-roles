FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:1c28bb8f639c1fd248a814910f2d222009a7396e4a82f43d6a6c254076be05ba

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
