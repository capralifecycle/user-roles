FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:5b99f2d0540abdf5b515c2b37363b341141f950230a450807d6ef8511809ce69

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
