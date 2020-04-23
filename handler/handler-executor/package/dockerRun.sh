#!/bin/bash

DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

IMAGE=fhexec:1.0.0
ARGS="-launchType local -externalLaunchType local -propFile /local/config/external.properties -payloadFile /local/payload.json"

docker run --env-file env.list -it      \
           -v ${DIR}/local:/local \
           -v ${DIR}/logs:/app/dumps    \
           ${IMAGE}                     \
           ${ARGS}
