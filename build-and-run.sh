#!/bin/bash
set -e

mvn package -DskipTests
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 target/app.jar
