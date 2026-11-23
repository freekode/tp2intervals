# Third Party to Intervals.icu

Copy workouts and training plans between TrainingPeaks, TrainerRoad, and
Intervals.icu. This fork of [freekode/tp2intervals](https://github.com/freekode/tp2intervals)
adds persistent duplicate protection and reviewed recovery for planned workouts
copied from TrainingPeaks to Intervals.icu. Original project and releases by
[freekode](https://github.com/freekode). Licensed under [GPL-3.0](LICENSE).
The original project's [support page](https://ko-fi.com/E1E6W6XZP) and
[community thread](https://forum.intervals.icu/t/tp2intervals-copy-trainingpeaks-and-trainerroad-workouts-plans-to-intervals/63375)
remain available.

## Install and configure

Use Docker Engine with Docker Compose v2 and a verified image built from this
fork. The current fork release was validated on `linux/amd64`. Use a matching
image for your host and validate other platforms separately. The
[upstream releases](https://github.com/freekode/tp2intervals/releases) include
desktop distributions, an executable JAR, and upstream container builds. Those
are separate from this fork's release artifacts.

For a local Compose installation, load the verified fork image first, then:

```sh
umask 077
cp .env.example .env
mkdir -p data
```

Set the loaded image reference in `.env`. Set a private preview bearer token if
you intend to run preview or review operations. For example, generate a secret
with `openssl rand -hex 32` and store it privately. Validate the configuration
without displaying resolved secrets, then start the service.

```sh
docker compose -f docker-compose.yaml config --quiet
docker compose -f docker-compose.yaml up -d
```

Open `http://127.0.0.1:8080`. The example binds the host port to loopback and
persists SQLite under `./data`. Use the [optional proxy overlay](docker-compose.proxy.yaml)
with Compose 2.24.4 or newer to remove the host port and attach to an existing
private proxy network. Verify the app's address and actual proxy peer before
using the overlay. The app itself listens on port 8080.

For a standalone executable JAR with the bundled web UI, use JDK 21:
`java -jar tp2intervals.jar`. It defaults to port 8080 and
`jdbc:sqlite:tp2intervals.sqlite`. To build from source, use JDK 21, Node.js and
the checked-in Gradle wrapper and npm lockfile. Build the UI with
`npm ci --prefix ui` and `npm run build --prefix ui`, copy the contents of
`ui/dist/ui/browser` into `boot/src/main/resources/static` in a build directory,
then run `(cd boot && ./gradlew bootJar)`. The repository Dockerfile packages
the resulting JAR. See [AGENTS.md](AGENTS.md) for test commands and restrictions
on live integration tests.

### Provider credentials

Enter credentials in **Configuration** on a trusted connection. Set writable
mode to save configuration, then return to preview-only if desired. Saved
credentials are shown as present or missing, with blank password inputs.
Leaving a stored credential input blank retains its value.

- **Intervals.icu:** enter the athlete ID and API key from
  [Developer Settings](https://intervals.icu/settings).
- **TrainingPeaks:** enter the `Production_tpAuth` cookie as a name/value
  pair. The browser's network inspector for the TrainingPeaks token request
  can show it. Keep it private.
- **TrainerRoad:** enter the `SharedTrainerRoadAuth` cookie as a name/value
  pair from a TrainerRoad request. TrainerRoad ramp steps remain unsupported.

### Runtime configuration

| Setting | Application default | Purpose |
|---|---|---|
| `TP2INTERVALS_PREFLIGHT_TOKEN` | unset | Enables bearer-protected preview, approval, hold resolution, and link repair. |
| `TP2INTERVALS_PREVIEW_ONLY` | `false` | Blocks provider writes, scheduled execution, ledger mutations, and mutating HTTP operations except the read-only preview POST. The generic Compose example defaults to `true`. Startup can still apply migrations. |
| `TP2INTERVALS_ENABLE_SCHEDULED_FORWARD_SYNC` | `false` | Separately enables unattended TrainingPeaks → Intervals.icu writes for approved account pairs. |
| `TP2INTERVALS_SCHEDULE_INTERVAL_MINUTES` | `60` | Forward cadence in whole minutes, from 5 to 1440. |
| `TP2INTERVALS_SCHEDULE_TIMEZONE` | `Europe/Amsterdam` | IANA zone for the forward schedule's rolling dates. |
| `SPRING_DATASOURCE_URL` | `jdbc:sqlite:tp2intervals.sqlite` | SQLite location. The Compose example uses `jdbc:sqlite:/data/tp2intervals.sqlite`. |
| `SERVER_FORWARD_HEADERS_STRATEGY` | `none` | Enable `native` only behind an explicitly trusted proxy. |
| `SERVER_TOMCAT_REMOTEIP_INTERNAL_PROXIES` | `(?!)` | Java regex for the trusted immediate proxy peer. The default matches nothing. |

`TP2INTERVALS_FORK_IMAGE`, `TP2INTERVALS_HOST_PORT`, and
`TP2INTERVALS_PROXY_NETWORK` are Compose example inputs. They are not
application properties.

### Upgrade and backup

Stop all writers and make a consistent SQLite backup, including committed WAL
contents. If moving from a file bind mount, use the SQLite backup to seed
`data/tp2intervals.sqlite`. Check for sidecars in the old container before
taking the backup. Verify the copy and rehearse the upgrade on disposable
data before changing the installed image. Fork migrations 0009–0011 add the
ledger, baseline and holds, and repair history without rewriting existing
records. The unchanged upstream migration 0008 rebuilds the schedule table,
so installations older than 0008 should inventory their schedules before
upgrading. Keep the latest ledger when switching images. Rolling back to older
code or older data can lose protections for remote writes. See the
[cutover and rollback runbook](docs/trainingpeaks-sync-ledger.md#first-deployment-with-existing-imports).

## TrainingPeaks synchronization

Planned calendar copy supports **TrainingPeaks → Intervals.icu** and
**Intervals.icu → TrainingPeaks**. TrainerRoad calendar copy supports
**TrainerRoad → Intervals.icu** and **TrainerRoad → TrainingPeaks**. TrainingPeaks
library and plan copy and TrainerRoad library-to-Intervals.icu remain
available subject to provider account permissions. The TrainingPeaks
calendar form offers Ride, MTB, Virtual Ride, Run, Swim, Walk, Weight Training,
and Any other.

The ledger applies to planned workouts copied from TrainingPeaks to
Intervals.icu. **Skip already synced** is mandatory in that direction,
including manual copy. Each claim is keyed by TrainingPeaks account,
Intervals.icu athlete, and source workout ID. Uncertain POSTs remain pending.
Ordinary synchronization is additive. It preserves manually edited events
and does not mirror later source edits, moves, or deletions. A missing
destination link remains protected until explicitly repaired.

TrainingPeaks Basic preview and scheduled forward sync use a rolling window
of today and tomorrow. Provider access and its day boundary can further
restrict the results. Scheduled runs fail closed on rejected reads and never
expand the window. Basic preview uses the backend's local date. Scheduled
forward sync uses its configured timezone. Provider limits on future writes
to TrainingPeaks also apply to the reverse direction.

### Review existing imports before activating scheduling

Start with preview-only enabled and scheduled forward sync disabled. Select
**TrainingPeaks → Intervals.icu**, choose the workout dates and types, enter
the preview bearer token, and choose **Preview sync (read-only)**. The token
authorizes the preview and three review operations below. It does not
authenticate the rest of the application. The preview reads source workouts,
destination events, and SQLite without creating links or calling a write API.

| Classification | Meaning |
|---|---|
| Verified link | The linked destination event was found and its marker matched. |
| Single legacy candidate | One historical match was found. Verify which account it belongs to before approving. |
| Multiple legacy candidates | More than one candidate matched. Review the events before considering recovery. |
| No match observed | The listings did not show a match. This does not prove absence. |
| Unresolved claim | A previous attempt has no confirmed outcome. Investigate before taking further action. |
| Broken or unverified link | The linked event is missing or changed, or the app cannot verify the relationship. |

For **Approve legacy baseline**, turn preview-only off, leave the forward
scheduler paused, and run a fresh preview. Select verified historical
candidates and explicitly acknowledge that missing matches are inconclusive.
Approval records local reconciliation decisions and holds. It makes no
provider changes. An approved baseline is required for unattended forward
sync. Unselected or unresolved entries remain protected as appropriate.

**Resolve unsynchronized hold** is an individual decision for an eligible,
currently visible held workout that you verified has never been synchronized.
**Repair missing link** is an individual decision for a previously confirmed
event that has disappeared. Repair relinks a single verified replacement
locally, or allows one recreation attempt after explicit verification and
acknowledgement. Both require a fresh preview, writable mode, the same
approved account pair, and scheduled forward sync paused. Leave an uncertain
result pending for investigation. [Detailed review and repair runbook](docs/trainingpeaks-sync-ledger.md)

## Schedule workouts

**Schedule for today** saves a recurring one-day request. **Schedule today +
tomorrow** is available for TrainingPeaks → Intervals.icu and saves a rolling
two-day request. Older requests retain their one-day window. Saving does not
start a sync. When active, the first scan can run shortly after startup, and
missed runs are not replayed. Forward sync defaults to every 60 minutes in
Europe/Amsterdam. Other directions retain their three-minute cadence and use
the JVM's timezone.

Run one scheduling instance. Requests survive restart in SQLite, and the
calendar page lists and deletes them. Edit a request by deleting and
recreating it. Stage a request with preview-only off and the forward gate
still disabled, and verify its paused status. Enable the forward gate
separately after reviewing the account and baseline. To pause, stop the app
if necessary, set the gate to false, recreate it, and verify the row stays
paused. Keep the ledger for any in-flight write investigation.
[Scheduling runbook](docs/trainingpeaks-sync-ledger.md#rolling-trainingpeaks-scheduling)

## Reverse proxies and secrets

Keep the app on loopback or a private access-controlled network. HTTPS
termination changes the scheme seen by the application. For same-origin
browser requests, including schedule DELETE, Tomcat must reconstruct the
original scheme, host, and port. Enable native forwarding only for the
exact immediate proxy peer and require the ingress to sanitize or overwrite
client-supplied forwarding headers. Broad proxy trust can let clients spoof
request metadata. Keep the existing CORS method checks, and leave the
application's backend port unpublished when using a proxy.
[Proxy example and deletion runbook](docs/trainingpeaks-sync-ledger.md#schedule-deletion-behind-https-termination)

The app is designed for trusted clients. The preview bearer is scoped to the
review routes, and ordinary sync and configuration depend on network access
controls. SQLite includes provider credentials and workout identifiers.
Protect `.env`, the database, and backups. Keep secrets out of Git, shell
history, application logs, screenshots, and HAR captures. Use
`docker compose config --quiet` or select only known public fields when
checking Compose. A resolved Compose dump can contain the bearer token.
If troubleshooting, share sanitized status and error codes and remove raw
provider bodies, account data, cookies, and Authorization headers.

## Follow-ups

Possible future work includes Traefik IP monitoring or static proxy addresses,
preview diagnostics UI improvements, explicit propagation of source edits,
moves or deletions, additional sync directions, and unrelated visual work.
