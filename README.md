# TrainingPeaks to Intervals.icu

App to sync planned workouts from TrainingPeaks to Intervals.icu and vice versa.

It can capture planned workouts from TP, then creates training plan in Intervals.icu based on these workouts.
And it can take today's or tomorrow planned workout and create it in TP, so you can sync it to your favourite
device.

**Only for educational purposes**

## Configuration

| Property                     | Price                                                          |
|------------------------------|----------------------------------------------------------------|
| `training-peaks.auth-cookie` | Cookie from `token` request to get authentication token for TP |
| `intervals.password`         | API password from Intervals.icu                                |
| `intervals.athlete-id`       | Athlete ID from Intervals.icu                                  |

### Authentication Cookie

How to get authentication cookie for your user:

1. Copy cookie `Production_tpAuth` from the browser on TP page 
2. Put cookie and value in the configuration, so it will look like this:
   ```yaml
   training-peaks:
    auth-cookie: Production_tpAuth=<very long string>
   ```
3. You are ready to go

## How to run

```shell
 docker run --rm -it \
   -e INTERVALS_PASSWORD=6ny7mjfubb5ey3wsv0kx7o4me \
   -e ATHLETE_ID=i55661 \
   tp2intervals \
   --plan-workout
```

## Known Issues

ATM only time duration based intervals in workouts are supported
