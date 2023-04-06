FROM azul/zulu-openjdk-alpine:19-jre@sha256:33f60e89d45fadac0647dc3a27730b583b541aa144fd97b3fc5809fe64644f9c

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
