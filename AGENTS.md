# AGENTS.md

## Project Structure

- `boot/` - Kotlin/Spring Boot backend (JDK 21) with SQLite, Liquibase, Spring Cloud OpenFeign
- `ui/` - Angular 17 frontend with Angular Material, SCSS
- `electron/` - Electron desktop app using electron-vite + electron-builder
- `cypress/` - End-to-end tests
- `jdktool/` - Utility for downloading JDK for Electron packaging
- `docker-compose.yml` - Docker deployment config

## Build Commands

### Backend
```bash
cd boot && ./gradlew build      # Build JAR + run tests
cd boot && ./gradlew test       # Run unit tests only
cd boot && ./gradlew jar         # Build JAR without tests
```

### Frontend
```bash
cd ui && npm install && npm run build
```

### Electron App
```bash
cd electron && npm run build     # Builds UI, downloads JDK, packages app
cd electron && npm run start      # Dev mode (runs UI + Electron)
```

### End-to-End Tests
```bash
java -jar boot/build/libs/tp2intervals.jar &   # Start app first
npm test --prefix cypress
```

## Key Facts

- JAR output: `boot/build/libs/tp2intervals.jar`
- UI is built to `ui/dist/ui/browser` and served by Spring Boot
- Version: read from `boot/version`
- Backend tests use WireMock (files in `config/mock/`)
- **Unit tests named `*Test.kt`**
- **Integration tests named `*IT.kt`**
- Frontend uses SCSS, Angular Material, proxy config at `ui/src/proxy.conf.json`
- Electron app downloads JDK dynamically during build (platform-specific)
