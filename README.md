[![Build branches](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml/badge.svg)](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml)
[![release](https://img.shields.io/github/release/freekode/tp2intervals)](https://github.com/freekode/tp2intervals/releases/latest)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/E1E6W6XZP)

# Third Party to Intervals.icu

App to sync workouts between TrainingPeaks, TrainerRoad and Intervals.icu.

### TrainingPeaks features

#### For athlete account

* Sync planned workouts from Intervals to TrainingPeaks for today and tomorrow (free TP account)
* Copy whole training plan from TrainingPeaks
* Copy planned workouts for date range from TrainingPeaks to Intervals.icu training plan or workout folder

#### For coach account

* Copy whole training plan and workout library from TrainingPeaks

### TrainerRoad features

* Copy workouts from TrainerRoad library to Intervals
* Copy planned workouts for date range from TrainerRoad to Intervals.icu training plan or workout folder

Executables for MacOS (DMG), Windows (EXE installer), Linux (AppImage) are available for
download [here](https://github.com/freekode/tp2intervals/releases/latest)

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tp.png?raw=true" width="30%"><img src="https://github.com/freekode/tp2intervals/blob/main/docs/tr.png?raw=true" width="30%">

**Only for educational purposes**

## Other ways to run the app

### Executable JAR

The project has executable jar with web UI. It requires JDK 21. To run jar:
```shell
java -jar tp2intervals.jar
```

By default, UI is available on `http://localhost:8080`. To change port start jar with parameter:
```shell
java -Dserver.port=9090 -jar tp2intervals.jar
```

### Docker

Docker image also built for every release

To run docker execute:

```shell
docker run --rm --name tp2intervals -p 8080:8080 ghcr.io/freekode/tp2intervals/tp2intervals:latest
```

Or with `docker-compose`

```yaml
version: '3.1'
services:
  app:
    image: ghcr.io/freekode/tp2intervals/tp2intervals:latest
    container_name: tp2intervals
    ports:
      - '8080:8080'
```

## How to configure

After you successfully started the application and were able to open the web UI page.
You need to configure it to gain access to Intervals.icu and to your other platform.
Nice post how to do it is written here https://forum.intervals.icu/t/implemented-push-workout-to-wahoo/783/87

### Intervals API Key and Athlete Id

These values available on [Settings page](https://intervals.icu/settings) in Developer Settings section.

### TrainingPeaks Auth Cookie

If you want ot use TrainingPeaks you need to configure it. Copy cookie `Production_tpAuth` (key and value, smth
like `Production_tpAuth=very_long_string`) from the browser on TrainingPeaks page.

### TrainerRoad Auth Cookie

If you want to use TrainerRoad you need to configure it. Very similar to TrainingPeaks. Copy cookie `TrainerRoadAuth` (key
and value, smth like `TrainerRoadAuth=very_long_string`) from the browser on TrainerRoad page.

After you gathered all required configuration, you can click Confirm button.
If everything is fine, you will be redirected to the home page.

If your configuration is wrong. You will see an error that there is no access to particular platform.
Check all your values and save configuration again.


## FAQ

* Only duration based steps in workouts are supported, the app can't work with distance based steps
* Ramp steps in TrainerRoad are not supported
* **MacOS** app is not signed. Usually you need to open it twice. After opening it, be patient, it takes some time to
  start
* **Windows** The app will ask to access local network and Internet allow it. After all it makes HTTP requests
* In case of any problems. You can create an issue
  in [GitHub](https://github.com/freekode/tp2intervals/issues)
  or write directly to me iam@freekode.org. Add logs from your app, it can help a lot to resolve the issue
* More info you can find on the forum https://forum.intervals.icu/t/tp2intervals-copy-trainingpeaks-and-trainerroad-workouts-plans-to-intervals/63375

### Scheduling for the next day with TrainingPeaks free account
Officially you can't plan workouts for future dates, but practically you can plan workout for tomorrow with free TP account.
TP server local time is in UTC-6 time zone. You can plan workout for the next day relative to TP server local time.

E.g your TZ is UTC+2, current local datetime 20.05.2024 06:00. TP server local datetime is 19.05.2024 22:00. You can plan workouts for 20.05.2024, you can't plan workouts for 21.05.2024, you can do it in 2 hours.

E.g your TZ is UTC+12, current local datetime 20.05.2024 18:00. TP server local datetime is 20.05.2024 00:00. You can plan workouts for 21.05.2024.

Example with [worldtimebuddy](https://www.worldtimebuddy.com/?pl=1&lid=206,100,756135,2193733&h=206&hf=0)

### How to get logs for your issue

1. Go to Configuration, check Show advanced configuration
2. Set Log Level to DEBUG, click Confirm
3. Reproduce your issue
4. Find log file according to your system

* Linux: ~/.config/tp2intervals/logs/main.log
* MacOS: ~/Library/Logs/tp2intervals/main.log
* Windows: %USERPROFILE%\AppData\Roaming\tp2intervals\logs\main.log
* JAR: ./tp2intervals.log
