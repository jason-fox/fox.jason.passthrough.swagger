#!/bin/sh
# This file is part of the  DITA-OT Swagger Plug-in project.
set -e

curl -sLO https://github.com/jgm/pandoc/releases/download/2.18/pandoc-2.18-1-amd64.deb
dpkg -i pandoc-2.18-1-amd64.deb
pandoc --version
echo
which pandoc

apt-get update -q
export DEBIAN_FRONTEND=noninteractive
apt-get install -qy --no-install-recommends nodejs
nodejs -v