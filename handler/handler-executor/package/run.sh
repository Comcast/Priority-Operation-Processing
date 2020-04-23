#!/usr/bin/env bash

##########################################################################
# Exports whatever variables are ndeeded


DOCKER_PROD=
export DOCKER_PROD
DOCKER_LAB=
export DOCKER_LAB

# build the image
mvn -U clean install

./dockerRun.sh $@
