[![Build branches](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml/badge.svg)](https://github.com/freekode/tp2intervals/actions/workflows/branch.yml)
[![release](https://img.shields.io/github/release/freekode/tp2intervals)](https://github.com/freekode/tp2intervals/releases/latest)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/E1E6W6XZP)

# Third Party to Intervals.icu

App to sync workouts between TrainingPeaks, TrainerRoad and Intervals.icu.

Runs on MacOS (DMG), Windows (EXE installer), Linux (AppImage). Alternatively there is Docker image and executable JAR.

All files are available for download on [Release page](https://github.com/freekode/tp2intervals/releases/latest).


* [List of features](#list-of-features)
    + [TrainingPeaks features](#trainingpeaks-features)
    + [TrainerRoad features](#trainerroad-features)
* [Configuration](#configuration)
    + [Intervals.icu](#intervalsicu)
    + [TrainingPeaks](#trainingpeaks)
    + [TrainerRoad](#trainerroad)
* [Other ways to run the app](#other-ways-to-run-the-app)
    + [Executable JAR](#executable-jar)
    + [Docker](#docker)
* [FAQ](#faq)
    + [General](#general)
    + [Sync automatically planned workouts to TrainingPeaks](#sync-automatically-planned-workouts-to-trainingpeaks)
    + [Info regarding scheduling for the next day with TrainingPeaks free account](#info-regarding-scheduling-for-the-next-day-with-trainingpeaks-free-account)
    + [How to export HAR file](#how-to-export-har-file)
    + [How to get logs for your issue](#how-to-get-logs-for-your-issue)



**TrainerRoad Updates ⚠️**

I don't have access to TrainerRoad anymore. Account, which I used, cancelled subscription. I don't use the platform and it's too expensive to have it for occasional fixes.
To fix issues I can only relay on logs and HAR files from you.

## List of features

### TrainingPeaks features
**Athlete account**
* Sync planned workouts from Intervals to TrainingPeaks for today and tomorrow (free TP account)
* Copy whole training plan from TrainingPeaks
* Create training plan or workout folder on Intervals from planned workouts on TrainingPeaks

**Coach account**
* Copy whole training plan and workout library from TrainingPeaks

### TrainerRoad features
* Copy workouts from TrainerRoad library to Intervals
* Create training plan or workout folder on Intervals from planned workouts on TrainerRoad

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tp.png?raw=true" width="30%"><img src="https://github.com/freekode/tp2intervals/blob/main/docs/tr.png?raw=true" width="30%">

**Only for educational purposes**

## Configuration

Before using the application you need to configure access to platforms.
Access to Intervals.icu is required, access to other platforms is optional.

After you gathered all required configuration, you can click Confirm button.
If everything is fine, you will be redirected to the home page.

If your configuration is wrong. You will see an error that there is no access to particular platform.
Check all your values and save configuration again.

### Intervals.icu
Copy API key and Athlete Id from [Settings page](https://intervals.icu/settings) in Developer Settings section on Intervals.icu web page.

### TrainingPeaks
To use TrainingPeaks Copy cookie `Production_tpAuth` (key and value, smth like `Production_tpAuth=very_long_string`) from the browser on TrainingPeaks page.
Another guide is available here https://forum.intervals.icu/t/implemented-push-workout-to-wahoo/783/87

<img src="https://github.com/freekode/tp2intervals/blob/main/docs/tp_guide.jpg?raw=true">

### TrainerRoad
Configuration is very similar to TrainingPeaks. Copy cookie `SharedTrainerRoadAuth` (key
and value, smth like `SharedTrainerRoadAuth=very_long_string`) from the browser on TrainerRoad page.


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

## FAQ

### General
* Ramp steps in TrainerRoad are not supported
* **MacOS** app is not signed. Usually you need to open it twice. After opening it, be patient, it takes some time to
  start
* **Windows** The app will ask to access local network and Internet, you need to allow it. After all it makes HTTP requests
* In case of any problems. You can create an issue on [GitHub](https://github.com/freekode/tp2intervals/issues)
  or write directly to me iam@freekode.org. Add logs from your app, it can help a lot to resolve the issue. Or in case of TrainerRoad create HAR file
* More info you can find on the forum https://forum.intervals.icu/t/tp2intervals-copy-trainingpeaks-and-trainerroad-workouts-plans-to-intervals/63375

### Sync automatically planned workouts to TrainingPeaks
If you are using app in docker container, you can set up automatic sync of planned workouts for TrainingPeaks.

Run command on your machine:
```sh
docker exec -it <container name> ln -s /scripts/sync-planned-to-tp.sh /etc/periodic/daily/
```
Script `sync-planned-to-tp.sh` will be executed at 02:00 everyday.
You can also edit crontab configuration manually and set your own schedule.

### Info regarding scheduling for the next day with TrainingPeaks free account
Officially if you have a free TP account, you can't plan workouts for future dates, but practically you can.
You can plan a workout for the next day relative to TrainingPeaks server local time. The server is in UTC-6 time zone. Let's check some examples:

Example 1. Your TZ is UTC+2 and current local date time 20.05.2024 06:00. It means at this moment TP server local date time is 19.05.2024 22:00.
Therefore, you can plan workouts for 20.05.2024. But you can't plan workouts for 21.05.2024, you can do it in 2 hours, because in 2 hours server local time will be 20.05.2024 00:00.

Example 2. Your TZ is UTC+12, current local date time 20.05.2024 18:00. TP server local date time is 20.05.2024 00:00. At this moment, you can plan workouts for 21.05.2024.

Visible time difference with [worldtimebuddy](https://www.worldtimebuddy.com/?pl=1&lid=206,100,756135,2193733&h=206&hf=0)

### How to export HAR file
1. Open new tab in your browser
2. Open dev tools, check Preserve log (Firefox Cog -> Persist Logs)

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-1.png?raw=true" width="70%">
3. Next steps are very important, as they simulate what the app does.
   Open TrainerRoad page, open workout library, find any workout, open workout page (the page where you have chart with workout steps, description, alternatives, etc.)
4. In dev tools, click Export HAR (Firefox - Cog -> Save All as HAR), save the file and send it to me

   <img src="https://github.com/freekode/tp2intervals/blob/main/docs/har-2.png?raw=true" width="70%">


### How to get logs for your issue
1. Go to Configuration
2. In General section check Debug Mode, click Confirm
3. Reproduce your issue
4. Find log file according to your system

* Linux: ~/.config/tp2intervals/logs/main.log
* MacOS: ~/Library/Logs/tp2intervals/main.log
* Windows: %USERPROFILE%\AppData\Roaming\tp2intervals\logs\main.log
* JAR: ./tp2intervals.log
