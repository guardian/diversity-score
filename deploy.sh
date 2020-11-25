#!/bin/sh

CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
pushd "$CURRENT_DIR" > /dev/null || exit 1

commitId=$(git rev-parse --short HEAD)
sbt assembly
docker build -t "702972749545.dkr.ecr.eu-west-1.amazonaws.com/amy-diversity-score:latest" -t "amy-diversity-score:$commitId" -t "702972749545.dkr.ecr.eu-west-1.amazonaws.com/amy-diversity-score:$commitId" .
echo $(aws ecr get-login-password --region eu-west-1 --profile developerPlayground) | docker login --password-stdin -u AWS https://702972749545.dkr.ecr.eu-west-1.amazonaws.com
docker push "702972749545.dkr.ecr.eu-west-1.amazonaws.com/amy-diversity-score"
popd