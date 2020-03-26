#!/usr/bin/env bash

##########################################################################
# Exports whatever variables are ndeeded


DOCKER_PROD=docker-prod.repo.theplatform.com
export DOCKER_PROD
DOCKER_LAB=docker-lab.repo.theplatform.com
export DOCKER_LAB

# build the image
mvn -U clean install

./dockerRun.sh $@
