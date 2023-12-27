FROM azul/zulu-openjdk-alpine:17-jre-headless@sha256:a941884f9cd6e586fcd78bc2b2082d43e1f59c9627df2957382f3379e35acc6a

RUN set -eux; \
    adduser -S app

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
