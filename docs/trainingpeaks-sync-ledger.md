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

Manual calendar copy reports affected workouts in the optional `syncReview`
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

## First deployment with existing imports

1. Pause scheduled and manual writers during upgrade and inventory. Keep a
   consistent SQLite backup with the existing configuration and schedule data.
   Liquibase adds the ledger table without rewriting those rows. Remote events
   are untouched by the migration.
2. Inventory relevant Intervals.icu calendars with read-only queries over the
   dates where workouts may have moved. Group linked events by destination
   athlete and the `trainingPeaksId` in the description footer. Verify the
   original source account for older imports, which lack the account marker.
   A date, name or duration alone cannot establish identity.
3. For a verified one-to-one match, fetch the remote event by ID and seed a
   `CONFIRMED` row in the new ledger under the full key. Record an `AMBIGUOUS`
   row with a null remote event ID when the workout key is known but the
   remote match is uncertain or there are multiple matches. Preserve all
   remote events. Leave automation paused for scope that cannot be inventoried
   reliably. In-window legacy links can also be adopted on demand by sync
   under the bounded lookup described above.
4. Review the links and outstanding rows before restoring schedules. Resolve
   ambiguous or missing events through explicit operator review. A timeout,
   an empty listing or elapsed time is insufficient evidence to clear a
   pending row for another POST.

The in-process guard covers overlapping scheduled and manual calls within
one app. Separate instances must point to the same SQLite file, using storage
with reliable SQLite locking, to share claims. Different SQLite files cannot
coordinate. Run one writer per destination when sharing cannot be guaranteed.
Preserve the ledger when moving or restoring an installation. Restoring a
backup that predates a remote write can lose its claim, so re-inventory before
restoring automatic writes in that case.

Pause sync when switching account configuration and restart before resuming so
the existing TrainingPeaks user and token caches are refreshed.
