FROM azul/zulu-openjdk-alpine:21@sha256:c7400a191b1fc9e06f5cc7e6ec0fe91f8cf112286619bfbe922f5e0bc2cba0ce

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
