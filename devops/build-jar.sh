#!/bin/bash

cd ./ui
npm ci
npm run build

cd ../
cp -r ui/dist/ui/browser boot/src/main/resources/static

cd ./boot
./gradlew build
