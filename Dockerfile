FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:0299878c810ee3e07290254952fe4853e123fd40d069ef84f399e5482e921e2a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
