# TrainingPeaks to Intervals.icu

App to sync planned workouts from TrainingPeaks to Intervals.icu and vice versa.

It can capture planned workouts from TP, then creates training plan in Intervals.icu based on these workouts.
And it can take today's or tomorrow planned workout and create it in TP, so you can sync it to your favourite
device.

**Only for educational purposes**

## How to run

### JAR
1. You need Java 21. The project has executable jar.
   To start it first of all you need to have Java 21. You can install any JDK, links to installation instructions 
   [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html), 
   [OpenJDK](https://jdk.java.net/21/),
   [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
2. Download project jar from the [latest release](https://github.com/freekode/tp2intervals/releases/latest)
3. Run the command
    ```shell
    java -jar tp2intervals.jar
    ```
   
   If you want to have ui on different port
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

## TrainingPeaks Authentication Cookie

How to get authentication cookie for your user:

1. Copy cookie `Production_tpAuth` (key and value, smth like `Production_tpAuth=very_long_string`) from the browser on TrainingPeaks page 
2. Save cookies on config page

## Known IssuesATM

Only time duration based intervals in workouts are supported
