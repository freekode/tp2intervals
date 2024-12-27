[![Build branches](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml/badge.svg)](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml)
[![release](https://img.shields.io/github/release/freekode/tp2intervals)](https://github.com/freekode/tp2intervals/releases/latest)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/E1E6W6XZP)

# Third Party to Intervals.icu

App to sync workouts between TrainingPeaks, TrainerRoad, Strava (activities only) and Intervals.icu.

Runs on MacOS (DMG), Windows (EXE installer), Linux (AppImage). Alternatively there is Docker image and executable JAR.

All files are available for download on [Release page](https://github.com/freekode/tp2intervals/releases/latest).

### TrainerRoad Updates ⚠️

I don't have access to TrainerRoad anymore. Account, which I used, cancelled subscription. I don't use the platform and it's too expensive to have it for occasional fixes.
To fix issues I can only relay on logs and HAR files from you.

### TrainingPeaks features

#### For athlete account

* Sync planned workouts from Intervals to TrainingPeaks for today and tomorrow (free TP account)
* Copy whole training plan from TrainingPeaks
* Create training plan or workout folder on Intervals from planned workouts on TrainingPeaks

#### For coach account

* Copy whole training plan and workout library from TrainingPeaks

### TrainerRoad features

* Copy workouts from TrainerRoad library to Intervals
* Create training plan or workout folder on Intervals from planned workouts on TrainerRoad

### Strava features

* Sync activities. App exports original file from Strava and uploads it to Intervals.icu. Only recorded activities are supported. 

### Beta features

Beta features can be enabled in Configuration

* Step Modifier (TrainingPeaks). Based on selecton `power=1s`, `power=3s` will be added for each step on Intervals.
* Remove HTML tags from description (TrainerRoad). Cleans up workouts descriptions from HTML tags.

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

Or with `docker compose`

```yaml
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

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tp_guide.jpg?raw=true">

### TrainerRoad Auth Cookie

If you want to use TrainerRoad you need to configure it. Very similar to TrainingPeaks. Copy cookie `SharedTrainerRoadAuth` (key
and value, smth like `SharedTrainerRoadAuth=very_long_string`) from the browser on TrainerRoad page.

After you gathered all required configuration, you can click Confirm button.
If everything is fine, you will be redirected to the home page.

If your configuration is wrong. You will see an error that there is no access to particular platform.
Check all your values and save configuration again.

### Strava Cookie

For using Strava just copy all your cookies from browser for Strava. It should be enough.

### Sync automatically planned workouts to TrainingPeaks

If you are using app in docker container, you can set up automatic sync of planned workouts for TrainingPeaks.

Run command on your machine:
```sh
docker exec -it <container name> ln -s /scripts/sync-planned-to-tp.sh /etc/periodic/daily/
```
Script `sync-planned-to-tp.sh` will be executed at 02:00 everyday.
You can also edit crontab configuration manually and set your own schedule.

## FAQ

* Only duration based steps in workouts are supported, the app can't work with distance based steps
* Ramp steps in TrainerRoad are not supported
* **MacOS** app is not signed. Usually you need to open it twice. After opening it, be patient, it takes some time to
  start
* **Windows** The app will ask to access local network and Internet, you need to allow it. After all it makes HTTP requests
* In case of any problems. You can create an issue on [GitHub](https://github.com/freekode/tp2intervals/issues)
  or write directly to me iam@freekode.org. Add logs from your app, it can help a lot to resolve the issue. Or in case of TrainerRoad create HAR file
* More info you can find on the forum https://forum.intervals.icu/t/tp2intervals-copy-trainingpeaks-and-trainerroad-workouts-plans-to-intervals/63375

### Info regarding scheduling for the next day with TrainingPeaks free account
Officially you can't plan workouts for future dates, but practically you can plan workout for tomorrow with free TP account.
You can plan workout for the next day relative to TP server local time. The server is in UTC-6 time zone.

E.g your TZ is UTC+2, current local datetime 20.05.2024 06:00. TP server local datetime is 19.05.2024 22:00. You can plan workouts for 20.05.2024, you can't plan workouts for 21.05.2024, you can do it in 2 hours.

E.g your TZ is UTC+12, current local datetime 20.05.2024 18:00. TP server local datetime is 20.05.2024 00:00. You can plan workouts for 21.05.2024.

Example with [worldtimebuddy](https://www.worldtimebuddy.com/?pl=1&lid=206,100,756135,2193733&h=206&hf=0)

### How to export HAR file

1. Open new tab in your browser
2. Open dev tools, check Preserve log (Firefox Cog -> Persist Logs)

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-1.png?raw=true" width="70%">
3. Next steps are very important, as they simulate what the app does.
   Open TrainerRoad page, open workout library, find any workout, open workout page (the page where you have chart with workout steps, description, alternatives, etc.)
4. In dev tools, click Export HAR (Firefox - Cog -> Save All as HAR), save the file and send it to me

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-2.png?raw=true" width="70%">


### How to get logs for your issue

1. Go to Configuration, check Show advanced configuration
2. Set Log Level to DEBUG, click Confirm
3. Reproduce your issue
4. Find log file according to your system

* Linux: ~/.config/tp2intervals/logs/main.log
* MacOS: ~/Library/Logs/tp2intervals/main.log
* Windows: %USERPROFILE%\AppData\Roaming\tp2intervals\logs\main.log
* JAR: ./tp2intervals.log
