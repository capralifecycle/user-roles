FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:57de337ca502296c19250cf2391b7081a0bc2ac3ab1f53b4855ebab93282a28c

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
