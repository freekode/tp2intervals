#!/bin/bash

npm ci --prefix ui
npm run build  --prefix ui

cp -r ui/dist/ui/browser boot/src/main/resources/static

cd ./boot
./gradlew build
