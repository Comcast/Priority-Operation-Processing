#!/bin/bash

DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

IMAGE=cppull:1.0.0
ARGS="-launchType local -externalLaunchType local -propFile /config/external.properties -confPath /config/conf.yaml"

docker run --env-file env.list -it      \
           -v ${DIR}/local/config:/config \
           -v ${DIR}/logs:/app/dumps    \
           ${IMAGE}                     \
           ${ARGS}
