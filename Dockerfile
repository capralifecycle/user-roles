FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:0fa52089a60f56d3212587189630276f7700b728bc9fe4aa9ea11ad46189cd43

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
