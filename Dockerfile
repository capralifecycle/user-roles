FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:42f047a2f0736e533a632c193da8e016bb1d642440b02f0bdc884051dfd3c810

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
