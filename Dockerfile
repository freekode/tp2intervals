FROM amazoncorretto:21-alpine
EXPOSE 8080

ARG JAR_PATH='boot/build/libs/tp2intervals.jar'
COPY $JAR_PATH app.jar
ENTRYPOINT java -jar /app.jar
