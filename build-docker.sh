#!/bin/bash
set -e

mvn package -DskipTests
docker build -t user-roles .
