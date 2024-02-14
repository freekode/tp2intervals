[![build](https://github.com/freekode/tp2intervals/actions/workflows/build.yml/badge.svg)](https://github.com/freekode/tp2intervals/actions/workflows/build.yml)
[![release](https://img.shields.io/github/release/freekode/tp2intervals)](https://github.com/freekode/tp2intervals/releases/latest)

# Third Party to Intervals.icu
Sync workouts and activities between TrainingPeaks and Intervals.icu.

Plan workouts for today and tomorrow from Intervals to TrainingPeaks.

**Only for educational purposes**

## How to start the app

### Executable for Linux / Windows / MacOS
Easiest way to run the app.

Download executable for your system from [latest release](https://github.com/freekode/tp2intervals/releases/latest) and run it.
The app will be automatically updated as soon as new version will be released.

### JAR
The project has executable jar with web UI. To run it perform these steps:

1. Install JDK 21
2. Download `tp2intervals.jar` from the [latest release](https://github.com/freekode/tp2intervals/releases/latest)
3. Run the command
    ```shell
    java -jar tp2intervals.jar
    ```
   
   To change web port run
    ```shell
    java -Dserver.port=9090 -jar tp2intervals.jar
    ```

4. By default UI is available on `http://localhost:8080`

### Docker
Docker image also built for every release

To run docker execute:
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

## FAQ
* Only duration based steps in workouts are supported, the app can't work with distance based steps 
* **MacOS** app is not signed. Usually you need to open it twice. After it opened, be patient, it takes some minutes to start.
* **Windows** The app will ask to access local network and Internet allow it. After all it makes HTTP requests.
* Contact me in case of any problems, create an issue in [GitHub](https://github.com/freekode/tp2intervals/issues) 
  or write directly to me <email:iam@freekode.org> and add logs from the app.

### Log location
* on Linux: ~/.config/tp2intervals/logs/main.log
* on macOS: ~/Library/Logs/tp2intervals/main.log
* on Windows: %USERPROFILE%\AppData\Roaming\tp2intervals\logs\main.log
