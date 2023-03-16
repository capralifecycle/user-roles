#!/bin/bash
set -e

./build-docker.sh

args=()

# If not checking for existence and file is missing, Docker would create a folder.
if [ -e overrides.properties ]; then
  args+=(-v)
  args+=("$PWD/overrides.properties:/overrides.properties")
fi

docker run -it --network=host --rm -e USE_LOCALSTACK=true user-roles
