#!/bin/bash

cd ./boot
./gradlew build -x test

cd ./electron
npm ci && npm run make
