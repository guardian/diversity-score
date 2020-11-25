#!/bin/sh

CURRENT_DIR="$( cd "$( dirname "$0" )" && pwd )"
pushd "$CURRENT_DIR" > /dev/null || exit 1

set -e
sbt assembly
docker build -t amy-diversity-score .
docker run --rm -it -v ~/.aws:/root/.aws -p 9000:9000 amy-diversity-score "$@"

popd > /dev/null || exit 1

#open browser with localhost:9000/healthcheck
