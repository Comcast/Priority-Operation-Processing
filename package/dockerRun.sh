#!/bin/bash

DIR=$(dirname $0)
[ "$DIR" = "." ] && DIR=$(pwd)

IMAGE=cphs:1.0.0
#IMAGE=$'docker-lab.repo.theplatform.com/feh:1.0.1'
CMD=spring-cpae-main.xml

docker run --env-file env.list -it      \
           -v ${DIR}/local/config:/config \
           -v ${DIR}/logs:/app/dumps    \
           ${IMAGE}                     \
           ${CMD}
