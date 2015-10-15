#!/bin/bash

fly \
  -t http://concourse.nono.com:8080 \
  execute \
  -c task.yml \
  -i blabbertabber=~/workspace/blabbertabber
