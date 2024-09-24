#!/bin/sh


if [ "$1" = "tomorrow" ]; then
    syncDate=$(date -d @$(( $(date +%s) + 86400 )) +%F)
else
    syncDate=$(date +%F)
fi

wget -O - \
  --header="Content-Type: application/json" \
  --post-data='{"startDate":"'$syncDate'","endDate":"'$syncDate'","types":["BIKE","VIRTUAL_BIKE","MTB","RUN"],"skipSynced":true,"sourcePlatform":"INTERVALS","targetPlatform":"TRAINING_PEAKS"}' \
  http://localhost:8080/api/workout/copy-calendar-to-calendar
