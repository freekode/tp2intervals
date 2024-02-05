#!/bin/bash

JAR_NAME=$1
VERSION=$2
INPUT_PATH=$3
OUTPUT_PATH=$4

jpackage \
  --name $JAR_NAME \
  --dest executable \
  --input $INPUT_PATH \
  --app-version $VERSION \
  --main-jar `ls $INPUT_PATH` \
  --type dmg
