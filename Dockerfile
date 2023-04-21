FROM azul/zulu-openjdk-alpine:19-jre@sha256:f4312e1d155b615300edcd90f9de5ba469fb3293b19b7fe3ed63b08fcc9e313d

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
