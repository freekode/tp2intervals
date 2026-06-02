# --- Stage 1: Build do Backend ---
FROM amazoncorretto:21-alpine AS backend-builder
WORKDIR /build-app
RUN apk add --no-cache findutils dos2unix
COPY . .

WORKDIR /build-app/boot
RUN dos2unix gradlew && chmod +x gradlew
RUN ./gradlew :bootJar --no-daemon

# --- Stage 2: Runner ---
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=backend-builder /build-app/boot/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]