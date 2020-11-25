#!/bin/sh

CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
pushd "$CURRENT_DIR" > /dev/null || exit 1

docker build -t amy-diversity-score -t 702972749545.dkr.ecr.eu-west-1.amazonaws.com/amy-diversity-score:latest .
docker tag amy-diversity-score:latest 702972749545.dkr.ecr.eu-west-1.amazonaws.com/amy-diversity-score:latest