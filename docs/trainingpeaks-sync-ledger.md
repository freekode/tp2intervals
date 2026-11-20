# TrainingPeaks calendar sync ledger

The ledger protects planned workouts copied from TrainingPeaks to Intervals.icu
with **Skip synced** enabled. Its key consists of the TrainingPeaks account ID,
Intervals.icu athlete ID, and TrainingPeaks workout ID. The existing API key is
used with the calendar create, list and lookup endpoints.

## What a sync run does

- An atomic SQLite insert commits a `PENDING` claim before the create request.
  No database transaction spans the network call. A successful response with an
  event ID records `CONFIRMED`. The next run checks the event by ID regardless
  of its calendar date. A missing event or changed link is reported for review.
- A timeout, lost response or process interruption leaves `PENDING`. A later
  run may confirm it only after finding a single event with matching workout
  and account markers and verifying it by ID. Calendar absence, even after
  several reads, never releases the claim. A claim records an attempt and
  carries no evidence of remote commit.
- For a workout with no row, sync reads the requested dates and a surrounding
  window extending 365 days on either side. It links a single legacy match
  after fetching it by ID. Multiple matches or a failed identity check are
  recorded as `AMBIGUOUS`. When both reads find no match, the workout is eligible
  for a first create. Both reads can miss a legacy event if it lies outside the
  window, lacks its description marker, or the API silently omits it.
- Sync preserves remote events and edits. **Skip synced** disabled still
  creates copies through the original path without using the ledger.

The calendar form displays manual copy warnings from the optional `syncReview`
response property. Automatic runs log a warning with the workout ID and reason.
Pending and ambiguous rows remain available for local review even after their
workouts leave the scheduler's date window. For example, inspect a protected
database backup with:

```sql
SELECT source_account_id, destination_athlete_id, source_workout_id,
       state, remote_event_id, created_at
FROM trainingpeaks_sync_ledger
WHERE state IN ('PENDING', 'AMBIGUOUS')
ORDER BY created_at;
```

A missing or changed event with a confirmed link stays `CONFIRMED` and is
reported when that workout is next examined. An unresolved attempt may require
operator investigation even if the POST never reached Intervals.icu.

## Read-only preview and schedule audit

Set `TP2INTERVALS_PREFLIGHT_TOKEN` on the backend to a high-entropy secret and
restart. Preview is disabled when the token is unset. In the calendar sync
form, choose **TrainingPeaks → Intervals.icu**, select the dates and workout
types, and enter the token for **Preview sync**. The form clears the input after
submission. Use localhost or HTTPS with a trusted reverse proxy. Protect the
token and avoid recording Authorization headers. The token is separate from
the provider credentials and protects only the preview endpoint. Keep the
app's other controls on a trusted local network or behind access controls.

The endpoint is `POST /api/workout/trainingpeaks-sync/preview` with
`Authorization: Bearer <preview token>` and a JSON body containing
`startDate`, `endDate` (ISO dates), and optionally `types` (workout-type enum
names). It uses only provider reads and ledger lookups, creates no claims or
links, and returns `Cache-Control: no-store`. A Basic athlete account can
preview today and tomorrow as measured by the backend's calendar day.
Provider availability and time zones can still limit the returned workouts.
The response includes an inventory revision, an account pair, and workout rows
with source and candidate IDs, dates where known, and reason codes. Submit the
reviewed selection, revision, acknowledgement and selected legacy links to
`POST /api/workout/trainingpeaks-sync/baseline/approve` using the same bearer. A positive candidate for a pending attempt is displayed
without confirming it. No match observed is inconclusive.

Schedule rows report unsafe, invalid, or paused states in the calendar form.
A forward schedule must contain the JSON boolean `true` for `skipSynced`.
False, missing, malformed, or ambiguous JSON cannot execute a forward job.
Correct an unsafe entry by deleting it and registering it again with
**Skip already synced workouts** checked. Manual force copy still works
and can bypass the ledger and create duplicates.

## First deployment with existing imports

1. Stop all writers and take a consistent SQLite backup, including data in
   any WAL sidecar. Keep the original database and the verified backup.
   Apply the additive migration on a disposable copy before cutover. Keep
   `TP2INTERVALS_ENABLE_SCHEDULED_FORWARD_SYNC=false` on every instance.
2. With preview-only enabled, run Preview sync over the supported source
   dates. Review the displayed account pair, six classifications, and any
   destination-marker or unresolved-ledger rows outside the source selection.
   Run additional read-only historical inventory where needed. Basic users
   can read today and tomorrow on the backend calendar. Review older imports
   independently on the destination when necessary.
3. For approval, restart with `TP2INTERVALS_PREVIEW_ONLY=false` and keep
   scheduled activation off on all instances. Preview again. Select a single
   legacy candidate only after verifying its source account. Acknowledge
   that no match observed is inconclusive and explicitly approve. The app
   rereads the inventory and rejects a changed revision. Approval persists
   the evidence, selected links, and holds together in SQLite. It does not
   update or delete provider events. Any stronger restriction on historical
   gaps should be resolved before this step.
4. Review the audit and safely enable the writer by setting
   `TP2INTERVALS_ENABLE_SCHEDULED_FORWARD_SYNC=true` only on the intended
   instances and restarting. The current account pair must have an approved
   baseline and a valid `skipSynced=true` schedule. Newly encountered IDs can
   sync automatically. Previously reviewed inconclusive or ambiguous
   workouts remain held, and pending attempts continue to require positive
   reconciliation. A blocked legacy row can be examined without repeating
   the whole approval for each new workout.

The in-process guard covers overlapping scheduled and manual calls within
one app. Separate instances must point to the same SQLite file, using storage
with reliable SQLite locking, to share claims. Different SQLite files cannot
coordinate. Run one writer per destination when sharing cannot be guaranteed.
Preserve the ledger when moving or restoring an installation. Restoring a
backup that predates a remote write can lose its claim, so re-inventory before
restoring automatic writes in that case.

Pause sync when switching account configuration and restart before resuming so
the existing TrainingPeaks user and token caches are refreshed.

## Preview-only Compose rehearsal

Apply [the preview override](../docker-compose.preview.example.yml) after the
installation's existing Compose file. It assumes the same `app` service key as
the repository example. If the installed key differs, use that key in a local
copy of the override. Keep the existing Traefik network, hostname, labels and
proxy settings in the base file. The override supplies no routing rules or
additional authentication. Retain any existing non-database mounts when
adapting the override. Compose 2.24.4 or newer supports `!override`.

1. Set `TP2INTERVALS_FORK_IMAGE` to the verified, locally available fork image
   reference. Use a digest or verify the local tag's image ID. The stock
   Compose file's upstream `:latest` image does not select the fork.
2. Use a dedicated directory for `TP2INTERVALS_DATA_DIR` containing
   `tp2intervals.sqlite`. For rehearsals, use disposable data. For cutover,
   pause writers, make and verify a consistent SQLite backup, and seed the
   new directory from that backup. Check for WAL and journal files in the
   old container's database directory before copying. If sidecars are visible
   only inside the old container, take the SQLite backup from a process
   that can see them. Keep the prior database and backup until validation.
3. Supply `TP2INTERVALS_PREFLIGHT_TOKEN` as a runtime secret. Validate
   composition without printing the resolved secret:

   ```sh
   docker compose -f /path/to/installed-compose.yml \
     -f /path/to/docker-compose.preview.example.yml config --quiet
   docker compose -f /path/to/installed-compose.yml \
     -f /path/to/docker-compose.preview.example.yml config --images
   ```

4. The overlay enables `TP2INTERVALS_PREVIEW_ONLY=true`. The server rejects
   mutating HTTP requests except the authenticated preview POST, rejects
   provider requests with non-read methods, skips scheduled jobs in both
   directions, and rejects ledger writes. Startup can still apply Liquibase
   migrations to the mounted database. Keep the app on the existing private
   LAN/WireGuard and Traefik routes. The preview token protects the preview
   route only. Never include credentials in access logs or HAR files.
5. Development uses sandbox `development-sandbox`, with backend port `8080`.
   With the existing host mapping, open `http://127.0.0.1:8080` on the Mac.
   For production, retain the installation's existing Traefik hostname.
6. To roll back, stop writers and save the current database directory with its
   ledger. Restore the prior image configuration and preserve that database.
   An older image can ignore both preview mode and write claims. Keep it
   isolated from providers until schedules and post-backup writes have been
   reconciled. Avoid dropping the ledger or replacing it with an older copy.

## Legacy baseline state and enforcement

- An installation starts without approval. Liquibase `0010` adds
  `trainingpeaks_sync_generation`, `trainingpeaks_legacy_baseline`, and
  `trainingpeaks_legacy_hold`. The audit row stores the source account,
  canonical destination athlete, configuration generation, revision,
  coverage dates, review decisions, approval time and state.
- Approval records an `APPROVED` row and supersedes any prior approved row.
  Its revision covers the reviewed date range, account identities, selected
  types, classifications, candidate IDs and dates, prior approval and
  SQLite inventory version. The provider reads happen before the SQLite
  transaction. SQLite rejects a changed generation, later approval, or
  ledger mutation between rechecking and recording the reviewed inventory.
- Changing a provider credential or the configured destination athlete
  advances the generation and marks existing approval `INVALIDATED`.
  Scheduled runs resolve the source account and destination identity.
  A different resolved pair invalidates the approval even when the stored
  configuration is unchanged. Returning to the old pair requires a new
  review. Provider token refresh, midnight and toggling activation do not
  advance the generation.
- The writer requires activation, a real boolean `skipSynced=true`,
  writable mode and an active approval for the resolved pair. The SQLite
  statement that claims a workout also checks current approval, generation
  and the absence of a hold. This check shares the same SQLite write
  boundary across instances. It rechecks approval before sending the POST
  and pins the destination authorization used to resolve the athlete.
  A revoked claim stays pending. A request already in flight can finish,
  so stop and drain all writers before configuration changes or rollback.
  Manual force-copy remains a separate deliberate action outside preview mode.
- Missing linked events stay linked for review. Claims never time out into
  an automatic retry. Baseline holds survive restarts and subsequent
  approvals. They can be reviewed manually using SQLite and preview.
  Preview alone neither confirms an unresolved claim nor clears a hold.
- Inventory and provider operations are not one distributed transaction.
  Both destination listings can omit an older import. Missing or edited
  markers and moves outside the bounded search can also conceal a historical
  event, especially with the Basic source window. A previously unobserved
  ID can therefore still look new. Keep the rollout paused if that residual
  risk is unacceptable for the reviewed inventory.
