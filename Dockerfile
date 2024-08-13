FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:0631ecf3bd3f6d4f3375017b16283318612adc2f70b9c7029c2c672c139de830

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
