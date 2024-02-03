#!/bin/sh

nginx -g 'daemon off;' & java -jar app.jar
