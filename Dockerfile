FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:74ea999488bd46602b29191c73c238cbdf6925700d30755f077372aca858923b

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
