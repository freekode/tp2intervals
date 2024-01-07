# TrainingPeaks to Intervals.icu

App to sync planned workouts from TrainingPeaks to Intervals.icu and vice versa.

It can capture planned workouts from TP, then creates training plan in Intervals.icu based on these workouts.
And it can take today's or tomorrow planned workout and create it in TP, so you can sync it to your favourite
device.

**Only for educational purposes**

## How to start the app

### JAR
1. You need Java 21. The project has executable jar.
   To start it first of all you need to have Java 21. You can install any JDK, links to installation instructions 
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
You need to have installed Docker engine, instruction how to install [you can find here.](https://docs.docker.com/engine/install/)

Next run the project with docker command:
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
After you successfully started the application and able UI web page.
You need to configure it to gain access to TrainingPeaks and to Intervals.icu

### TrainingPeaks Auth Cookie
Copy cookie `Production_tpAuth` (key and value, smth like `Production_tpAuth=very_long_string`) from the browser on TrainingPeaks page.

### Intervals API Key and Athlete Id
These values available on [Settings page](https://intervals.icu/settings) in Developer Settings section.

After you gathered all required configuration you can click Submit button on Configuration page.
If everything is fine you will be redirected to home page where you can plan your workouts on TrainingPeaks.

If your configuration is wrong. You will see an error that there is no access to TrainingPeaks or to Intervals.icu.
Check all your values and save configuration again.

## Known IssuesATM

Only time duration based intervals in workouts are supported
