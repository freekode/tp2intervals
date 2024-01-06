FROM nginx:alpine

RUN apk add --no-cache &&\
    wget -O /etc/apk/keys/amazoncorretto.rsa.pub https://apk.corretto.aws/amazoncorretto.rsa.pub && \
    echo "https://apk.corretto.aws" >> /etc/apk/repositories && \
    apk update && \
    apk add amazon-corretto-21

ARG JAR_NAME='tp2intervals.jar'

COPY ui/dist/ui/browser /usr/share/nginx/html
COPY boot/build/libs/$JAR_NAME app.jar

COPY docker/docker-start.sh /
COPY docker/default.conf /etc/nginx/conf.d

#EXPOSE 80

CMD ["/docker-start.sh"]
