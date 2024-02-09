#!/bin/bash

cd ./boot
./gradlew build

cd -

npm ci --prefix electron
npm ci --prefix ui

npm run make --prefix electron
