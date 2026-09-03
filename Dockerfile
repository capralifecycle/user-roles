FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:fb5ed0e24f05ebde345ebf359f9f75c9bd9f3f80c47482bdbb34ea470f49dd3d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
