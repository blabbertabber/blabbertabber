#!/bin/bash

set -eux

pushd blabbertabber/
  android list target
  echo no | android create avd --force -n test -t android-21 --abi armeabi-v7a
  emulator -avd test -no-skin -no-audio -no-window &
  sleep 90
  adb shell input keyevent 82 &
  ./gradlew connectedAndroidTest -PdisablePreDex
popd # completely useless
