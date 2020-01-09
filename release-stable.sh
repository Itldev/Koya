#!/bin/bash

ITLDEV_BIN=~/.itldev.bin
MVNRELEASE_GITSRC="ssh://git@srv-soft2.fichorgapms.local:itldev/maven-release.git"
MVNRELEASE_DIR="maven-release"

# update script maven release si nécessaire
PROJECT_DIR=`pwd`

cd $ITLDEV_BIN/$MVNRELEASE_DIR/
git pull
cd $PROJECT_DIR

echo "============ Création release =============="
 # 1 - Création de la release sur la branche
$ITLDEV_BIN/$MVNRELEASE_DIR/mavenrelease.py

if [ $? -ne 0 ];then
        exit $?
fi
echo "============ Push version stable  =============="

git push origin stable
git push --tags

MAVEN_CLI_OPTS= "-B -DskipTests=true"

mvn clean deploy $MAVEN_CLI_OPTS

echo "============ Retour sur la branche master  ======"
git checkout master

exit 0

