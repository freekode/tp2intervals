#!/bin/sh

# How to run
# ./sync-planned-workouts.sh (tomorrow|2025-01-01) (INTERVALS|TRAINING_PEAKS|TRAINER_ROAD) (INTERVALS|TRAINING_PEAKS|TRAINER_ROAD) [standalone]
#
# Example, sync planned workouts from TrainerRoad to TrainingPeaks for tomorrow when running standalone app
# ./sync-planned-workouts.sh tomorrow TRAINER_ROAD TRAINING_PEAKS standalone
#
# Example, sync planned workouts from Intervals to TrainingPeaks for January 5th when running in docker or JAR
# ./sync-planned-workouts.sh 2025-01-05 INTERVALS TRAINING_PEAKS


syncDate=
sourcePlatform=$2 # INTERVALS, TRAINING_PEAKS, TRAINER_ROAD
targetPlatform=$3 # INTERVALS, TRAINING_PEAKS, TRAINER_ROAD
port=8080 # 8080, 44864


tomorrow_date() {
    platform=$(uname)
    if [ "$platform" == "Darwin" ]; then
        echo $(date -v+1d +"%Y-%m-%d")
    elif [ "$platform" == "Linux" ]; then
        echo $(date -d @$(( $(date +%s) + 86400 )) +%F)
    fi
    return 0
}

if [ "$1" = "tomorrow" ]; then
    syncDate=$(tomorrow_date)
else
    syncDate=$1
fi

if [ "$4" = "standalone" ]; then
    port=44864
fi

postData='{"startDate":"'$syncDate'","endDate":"'$syncDate'","types":["BIKE","VIRTUAL_BIKE","MTB","RUN"],"skipSynced":true,"sourcePlatform":"'$sourcePlatform'","targetPlatform":"'targetPlatform'"}'

wget -O - \
  --header="Content-Type: application/json" \
  --post-data=$postData \
  http://localhost:$port/api/workout/copy-calendar-to-calendar
