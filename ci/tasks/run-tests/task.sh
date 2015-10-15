#!/bin/bash

set -eux

pushd blabbertabber/
ls -ld /proc/kvm

echo no | android create avd --force -n test -t android-23 --abi armeabi-v7a
emulator -avd test -no-skin -no-audio -no-window &
sleep 90
adb shell input keyevent 82 &
android list target
./gradlew connectedAndroidTest -PdisablePreDex
