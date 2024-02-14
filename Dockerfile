FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:0c4e3bd87c010ec81c5b11f3a7526c650cebaa64a944807c0415d421ec5706ab

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
