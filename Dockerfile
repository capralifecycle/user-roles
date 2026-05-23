FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:fd692ae3e801f365dc878fe1215c7ae4c6e2252eb8107f51abbdda10dfc5f27e

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
