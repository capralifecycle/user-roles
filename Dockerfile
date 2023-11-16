FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:bd3cc81da0b70216848897058902e96e325b60c54126b04dcbdab5fcdde07daa

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
