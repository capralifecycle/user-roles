FROM azul/zulu-openjdk-alpine:19-jre@sha256:0b8f2b83df861ee6758b9ec2e2b636a171f5280e737ba4a338f8bc733f2bb1d6

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
