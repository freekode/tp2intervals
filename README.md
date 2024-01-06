# TrainingPeaks to Intervals.icu

App to sync planned workouts from TrainingPeaks to Intervals.icu and vice versa.

It can capture planned workouts from TP, then creates training plan in Intervals.icu based on these workouts.
And it can take today's or tomorrow planned workout and create it in TP, so you can sync it to your favourite
device.

**Only for educational purposes**

## How to run

Run with `docker-compose`

### TrainingPeaks Authentication Cookie

How to get authentication cookie for your user:

1. Copy only cookie `Production_tpAuth` or all of them from the browser on TrainingPeaks page 
2. Save cookies on config page

## Known Issues

ATM only time duration based intervals in workouts are supported
