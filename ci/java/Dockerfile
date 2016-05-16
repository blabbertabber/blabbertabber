FROM fedora

MAINTAINER Brian Cunnie <brian.cunnie@gmail.com>

# Install openjdk because that's where Android N is going to be
RUN dnf update -y; \
  dnf install -y which git unzip vim vim-minimal \
      java-1.?.0-openjdk \
      java-1.?.0-openjdk-devel

