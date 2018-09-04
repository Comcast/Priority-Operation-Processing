#!/bin/bash

DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

IMAGE=fhpull:1.0.0
#IMAGE=$'docker-lab.repo.theplatform.com/feh:1.0.1'
ARGS="-externalLaunchType local -propFile ./handler/main/package/local/config/external.properties"

docker run --env-file env.list -it      \
           -v ${DIR}/local/config:/config \
           -v ${DIR}/logs:/app/dumps    \
           ${IMAGE}                     \
           -- ${ARGS}
