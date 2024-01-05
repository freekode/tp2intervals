# Third Party to Intervals.icu

App to sync planned workouts from Third Party to Intervals.icu and vice versa.

It can capture planned workouts from Third Party, then creates training plan in Intervals.icu based on these workouts.
And it can take today's or tomorrow planned workout and create it in Third Party, so you can sync it to your favourite
device.

**Only for educational purposes**

## Configuration

| Property                  | Price                                                                  |
|---------------------------|------------------------------------------------------------------------|
| `third-party.api-url`     | API url to ThirdParty service                                          |
| `third-party.auth-cookie` | Cookie from `token` request to get authentication token for ThirdParty |
| `intervals.password`      | API password from Intervals.icu                                        |
| `intervals.athlete-id`    | Athlete ID from Intervals.icu                                          |

### Authentication Cookie
How to get authentication cookie for your user:
1. Open your browser
2. Open Development tools (F12) and select Network tab
3. Go to ThirdParty service and log in, if you are not
4. Search in Network tab, request to `/users/v3/token`
5. Copy value of `Production_tpAuth` cookie from the request
6. Put key and value in the configuration, so it will look like this:
   ```yaml
   third-party:
    auth-cookie: Production_tpAuth=<very long string>
   ```
7. You are ready to go

## How to run
```shell
 docker run --rm -it \
   -e INTERVALS_PASSWORD=6ny7mjfubb5ey3wsv0kx7o4me \
   -e ATHLETE_ID=i55661 \
   tp2intervals \
   --plan-workout
```

## Known Issues
ATM only time duration intervals are supported
