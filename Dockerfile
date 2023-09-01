FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:7a3d65ba3757a536800f9333751275d4c6d1f78537feec313eb3d0768952a687

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
