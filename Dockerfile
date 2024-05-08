FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:863d0c190556d474c04d84dbccc592365185486b961f62e601c651dbcb0ef842

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
