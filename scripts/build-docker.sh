#!/bin/bash

IMAGE_NAME=$1
JAR_NAME=$2
VERSION=$3

cd ./ui
npm ci && npm run build

cd ../boot
./gradlew build

cd ../
docker build . --file Dockerfile --tag $IMAGE_NAME --build-arg JAR_NAME=$JAR_NAME-$VERSION.jar
