FROM azul/zulu-openjdk-alpine:21-jre-headless@sha256:9a657d6d26f18b9452b94f1ec6c4722ed6c392bb6f9fee7292a291d967ec210e

RUN set -eux; \
    adduser -S app

# Hotfix for https://emudhra.we-have-a-problem.com/
# This shouldn't be necessary once the base image stops shipping the vulnerable root certs
RUN keytool -delete -cacerts -storepass changeit -alias "emsigneccrootcag3 [jdk]" || true \
 && keytool -delete -cacerts -storepass changeit -alias "comodoaaaca [jdk]" || true \
 && ! keytool -list -v -cacerts -storepass changeit \
  | grep -E 'CN=(emSign ECC Root CA - G3|AAA Certificate Services),'

COPY target/app.jar /app.jar

EXPOSE 8080

USER app
WORKDIR /

CMD ["java", "-Dlogback.configurationFile=logback-container.xml", "-jar", "/app.jar"]
