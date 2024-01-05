FROM amazoncorretto:21-alpine
MAINTAINER freekode.org

ARG JAR_NAME='tp2intervals.jar'

COPY boot/build/libs/$JAR_NAME app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
