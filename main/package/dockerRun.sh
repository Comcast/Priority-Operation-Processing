#!/bin/bash

DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

IMAGE=fhpr:1.0.0
#IMAGE=$'docker-lab.repo.theplatform.com/feh:1.0.1'
ARGS="-launchType local -externalLaunchType local -propFile /local/config/external.properties -payloadFile /local/payload.json"

docker run --env-file env.list -it      \
           -v ${DIR}/local:/local \
           -v ${DIR}/logs:/app/dumps    \
           ${IMAGE}                     \
           ${ARGS}
