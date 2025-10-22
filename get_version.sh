#!/bin/sh
set -e
BASE_VERSION=$(grep '<revision>' pom.xml | sed -n 's/.*<revision>\([^<]*\)-SNAPSHOT<\/revision>.*/\1/p')
echo "BASE_VERSION=${BASE_VERSION}" >> $GITHUB_ENV
