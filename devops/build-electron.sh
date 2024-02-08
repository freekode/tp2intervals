#!/bin/bash

cd ./boot
./gradlew build -x test

cd -

cd ./electron
npm ci && npm run make
