# AGENTS.md

## Project Structure

- `boot/` - Kotlin/Spring Boot backend (JDK 21)
- `ui/` - Angular 17 frontend
- `electron/` - Electron desktop app wrapper
- `cypress/` - End-to-end tests

## Build Commands

### Backend
```bash
(cd boot && ./gradlew build)                 # Assemble and run all backend tests
(cd boot && ./gradlew test --tests '*Test')   # Current unit test classes
(cd boot && ./gradlew bootJar)               # Executable JAR, without tests
```

### Frontend
```bash
npm ci --prefix ui
npm run build --prefix ui
```

### Electron App
```bash
npm ci --prefix electron
npm run build --prefix electron   # Requires executable JAR staged as electron/artifact/boot.jar
```

The Electron build invokes the relative-base UI build. Electron dev startup
currently calls a missing UI `start` script (`ui` exposes `dev`).

### End-to-End Tests
```bash
npm ci --prefix cypress
java -jar boot/build/libs/tp2intervals.jar &   # Use a UI-bundled JAR and isolated test configuration
npm test --prefix cypress
```

## Key Facts

- JAR output: `boot/build/libs/tp2intervals.jar`
- UI is built to `ui/dist/ui/browser` and copied to `boot/src/main/resources/static` during CI
- Version: read from `boot/version`
- Backend tests use client fakes in `boot/src/test/kotlin/config/mock/` and WireMock for TrainerRoad. Some `*IT.kt` tests call live APIs and may modify or delete data. Do not run those tests without explicit authorization, and use disposable accounts.
- Frontend uses SCSS, Angular Material, proxy config at `ui/src/proxy.conf.json`

## Fork development

- For planned workout sync from TrainingPeaks to Intervals.icu, honor the Basic account's accessible calendar and surface provider limits. Treat the README's today/tomorrow note as boundary behavior to test with a fixed clock. The Premium warning for writes to TrainingPeaks concerns the reverse direction.
- Keep sync additive. Preserve source workouts and existing destination events, including ambiguous matches. Use stable TrainingPeaks workout identity scoped to source and destination accounts. With Skip synced enabled, repeated and overlapping runs must produce at most one Intervals.icu event per source workout. Reconcile uncertain writes before retrying.
- Mock both APIs for sync regressions. Use synthetic fixtures, fixed clocks and explicit barriers. Assert final destination contents and write counts for replay, a committed write with a lost response, partial failure and concurrent manual/scheduled runs.
- Redact cookies, API keys, authorization headers and personal data from diagnostics. Keep credentials, raw HAR files and live SQLite databases out of Git.
- Use JDK 21, the checked-in Gradle wrapper and lockfile installs with CI's Node version. Record checks run and checks skipped. Work on a feature branch and submit a PR. The owner controls merges, release tags, publication and deployment.
