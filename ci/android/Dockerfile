FROM cunnie/java

MAINTAINER Brian Cunnie <brian.cunnie@gmail.com>

ENV repo_path=https://raw.githubusercontent.com/blabbertabber/blabbertabber/develop/
ENV ANDROID_HOME=/opt/android
# check http://developer.android.com/sdk/index.html#downloads for latest SDK
ENV ANDROID_SDK_URI=http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz

RUN mkdir $ANDROID_HOME; cd $ANDROID_HOME; \
  mkdir -p gradle/wrapper app; \
  for file in \
    gradlew \
    gradle/wrapper/gradle-wrapper.jar \
    gradle/wrapper/gradle-wrapper.properties \
    app/build.gradle \
    build.gradle \
    settings.gradle; \
  do \
    curl -L ${repo_path}/${file} -o $file; \
  done; \
  curl -L $ANDROID_SDK_URI -o sdk.zip; unzip sdk.zip; \
  bash -x gradlew; 

## Install Deps
#RUN dpkg --add-architecture i386 && apt-get update && apt-get install -y --force-yes expect git wget libc6-i386 lib32stdc++6 lib32gcc1 lib32ncurses5 lib32z1 python curl
#
## Install Android SDK
#RUN cd /opt && wget --output-document=android-sdk.tgz --quiet http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz && tar xzf android-sdk.tgz && rm -f android-sdk.tgz && chown -R root.root android-sdk-linux
#
## Setup environment
#ENV ANDROID_HOME /opt/android-sdk-linux
#ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
#
## Install sdk elements
#COPY tools /opt/tools
#ENV PATH ${PATH}:/opt/tools
#RUN ["/opt/tools/android-accept-licenses.sh", "android update sdk --all --force --no-ui --filter platform-tools,tools,build-tools-21,build-tools-21.0.1,build-tools-21.0.2,build-tools-21.1,build-tools-21.1.1,build-tools-21.1.2,build-tools-22,build-tools-22.0.1,build-tools-23.0.2,android-21,android-22,android-23,addon-google_apis_x86-google-21,extra-android-support,extra-android-m2repository,extra-google-m2repository,extra-google-google_play_services,sys-img-armeabi-v7a-android-21"]
#
#RUN which adb
#RUN which android
#
## Create emulator
#RUN echo "no" | android create avd \
#                --force \
#                --device "Nexus 5" \
#                --name test \
#                --target android-21 \
#                --abi armeabi-v7a \
#                --skin WVGA800 \
#                --sdcard 512M
#
## Cleaning
#RUN apt-get clean

# GO to workspace
RUN mkdir -p /opt/workspace
WORKDIR /opt/workspace
