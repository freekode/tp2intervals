#!/bin/bash

cd ./boot
./gradlew build -x test

cd -

npm ci --prefix electron
npm ci --prefix ui

cd ./electron
npm run make
