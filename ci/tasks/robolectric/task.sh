#!/bin/bash

set -eux

cd blabbertabber/
export TERM=dumb # fixes `Could not open terminal for stdout: $TERM not set`
./gradlew test
