[![build](https://github.com/freekode/tp2intervals/actions/workflows/build.yml/badge.svg)](https://github.com/freekode/tp2intervals/actions/workflows/build.yml)
[![release](https://img.shields.io/github/release/freekode/tp2intervals)](https://github.com/freekode/tp2intervals/releases/latest)

# Third Party to Intervals.icu
Sync workouts and activities between TrainingPeaks and Intervals.icu.

Plan workouts for today and tomorrow from Intervals to TrainingPeaks.

**Only for educational purposes**

## How to start the app

### JAR
Easiest way to run the app.

1. You need JDK 21. The project has an executable jar.
   To start it first of all you need to have Java 21. You can install any JDK, links to installation instructions:
   [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html), 
   [OpenJDK](https://jdk.java.net/21/),
   [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
2. Download `tp2intervals.jar` from the [latest release](https://github.com/freekode/tp2intervals/releases/latest)
3. Run the command
    ```shell
    java -jar tp2intervals.jar
    ```
   
   If you want to have UI on different port
    ```shell
    java -Dserver.port=9090 -jar tp2intervals.jar
    ```

4. UI will be available on `http://localhost:8080` or on another port which you choose

### Docker
If you already have a Docker, that'll suit you more.

You need to have installed the Docker engine. Instructions on how to install [you can find here.](https://docs.docker.com/engine/install/)

Next, run the project with Docker command:
```shell
docker run -rm --name tp2intervals -p 8080:80 ghcr.io/freekode/tp2intervals/tp2intervals:latest
```

Or with `docker-compose`
```yaml
version: '3.1'
services:
  app:
    image: ghcr.io/freekode/tp2intervals/tp2intervals:latest
    container_name: tp2intervals
    ports:
      - '8080:80'
```

## How to configure
After you successfully started the application and were able to open the web UI page.
You need to configure it to gain access to Intervals.icu and to TrainingPeaks.

### TrainingPeaks Auth Cookie
Copy cookie `Production_tpAuth` (key and value, smth like `Production_tpAuth=very_long_string`) from the browser on TrainingPeaks page.

### Intervals API Key and Athlete Id
These values available on [Settings page](https://intervals.icu/settings) in Developer Settings section.

After you gathered all required configuration, you can click Submit button on Configuration page.
If everything is fine, you will be redirected to the home page, where you can plan your workouts on TrainingPeaks.

If your configuration is wrong. You will see an error that there is no access to TrainingPeaks or to Intervals.icu.
Check all your values and save configuration again.

## Known Issues
Only duration based intervals in workouts are supported
