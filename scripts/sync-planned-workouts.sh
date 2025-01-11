#!/bin/sh

syncDate=
sourcePlatform=$2
targetPlatform=$3
port=8080

if [ "$1" = "tomorrow" ]; then
    syncDate=$(date -d @$(( $(date +%s) + 86400 )) +%F)
else
    syncDate=$(date +%F)
fi

if [ "$1" = "electron" ]; then
    port=44864
fi

wget -O - \
  --header="Content-Type: application/json" \
  --post-data='{"startDate":"'$syncDate'","endDate":"'$syncDate'","types":["BIKE","VIRTUAL_BIKE","MTB","RUN"],"skipSynced":true,"sourcePlatform":"'$sourcePlatform'","targetPlatform":"'targetPlatform'"}' \
  http://localhost:$port/api/workout/copy-calendar-to-calendar
