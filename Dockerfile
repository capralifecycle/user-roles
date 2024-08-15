FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:2e18957aab194efce558131668f7026be59125eaba6b421ac7c6d3093a1f0cbb

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
