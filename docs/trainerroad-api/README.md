# TrainerRoad API Notes

This directory documents the unofficial TrainerRoad web API behavior used by tp2intervals.
TrainerRoad does not publish this API as a supported integration API, and TrainerRoad may change
it at any time. This documentation is for interoperability and debugging, not for bypassing
authentication or access controls.

**Legal and safety:** Review TrainerRoad's current Terms of Service and API terms before using or
redistributing captured data. Publish only data you are authorized to share. Do not commit cookies,
tokens, personal names, member IDs, workout descriptions, private URLs, HAR files, or credentials.
Use synthetic IDs and placeholder text in committed examples.

## OpenAPI description

The schema is at [openapi.json](openapi.json). It describes the web endpoints observed by this
project and marks fields as optional or unofficial. It is not an official TrainerRoad specification.

## Authentication

Requests use a browser-style cookie:

```text
Cookie: SharedTrainerRoadAuth=<redacted>
```

The application must send the cookie to the member, calendar, timeline, and workout-detail endpoints.
Never log or commit the cookie value. The test-connection result should expose only a username or a
non-sensitive error.

## Key endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/app/api/member-info` | Verify the cookie and resolve member ID and username |
| GET | `/app/api/react-calendar/{memberId}/timeline` | Read planned activities and completed activities |
| GET | `/app/api/calendar/activities/{username}` | Legacy calendar activities endpoint |
| GET | `/app/api/workoutdetails/{workoutId}` | Read workout metadata and interval data |
| GET | `/app/api/workouts` | Search workout library |

The legacy `/app/api/calendar/activities/{username}` endpoint is not assumed to remain available.
It previously returned 405 with `Allow: DELETE` during investigation. The react-calendar timeline
is the preferred source for planned workouts.

## Calendar dates and timestamps

Use separate representations:

- Planned workout date: `LocalDate`, for example `2026-11-24`
- Optional planned workout time: `PlannedActivities[].TimeOfDay`, for example `18:00:00`
- Intervals local event time: timezone-free ISO-8601 local datetime, for example
  `2026-11-24T18:00:00`
- Real instants: UTC/ISO-8601 with `Z` or an explicit offset

Do not convert a calendar date through the machine timezone. A calendar date is the same date
at both the source and destination.

## Planned and completed timeline entries

A read-only inspection of one account's timeline found planned workouts in
`PlannedActivities` and activity entries in `Activities`. The inspected examples included
an indoor TrainerRoad workout and an outdoor workout synced from Garmin. Their corresponding
titles were not present in `PlannedActivities` on those dates; activity entries on those dates
were plausible matches, but the timeline alone did not prove their identity or pairing.

An illustrative future planned entry included:

```json
{
  "WorkoutId": 12345,
  "Status": 0,
  "ManuallyCompleted": false,
  "Tss": 72,
  "ActualTss": 72
}
```

In that sample, every observed future planned entry had `ActualTss` equal to `Tss`, often
nonzero. **A nonzero `ActualTss` is not sufficient evidence of completion.** The inspected
activity entries included `Id`, `Started`, `EventType`, `IsOutside`, `Tss`, and `PlannedTss`,
but no `WorkoutId` or planned-event ID. Identifiers and values above are synthetic.

These observations are not an API contract: they do not establish that every completed workout
disappears from `PlannedActivities`, what each `Status` or `EventType` means, or that a missing
planned workout is a rest day. The two plausible completed entries had raw `Started` timestamps
two hours earlier than the times shown to the user; their timezone interpretation was not verified.
Other endpoints may expose associations not present in the timeline.

The current planned-workout sync reads `PlannedActivities` but does not check the `Activities`
list for completion. In particular, an Intervals.icu Mirror operation does not establish that a
stale planned event is unpaired or incomplete before updating or removing it.

In a separate read-only Intervals.icu check, future planned events exposed `paired_activity_id`,
and a completed activity exposed `paired_event_id`. The observed values were null for the
unpaired records. In a separate live synthetic test, a planned event was created first and a
matching simulated activity uploaded afterwards. Intervals paired them, and both fields referred
to the corresponding record on subsequent reads. These fields are not included in the current
Intervals reconciliation DTO. Their behavior in other cases and during concurrent updates still
needs verification before relying on them as a completion-safety check.

In one live test, a synthetic planned event was created after a completed indoor activity
already existed on the same date and time. Both pairing fields remained null on subsequent
reads. This single activity-first test does not establish whether pairing may occur later
or in other circumstances.

## Workout payload mapping

The useful workout details response contains:

- `Workout.Details.Id`
- `Workout.Details.WorkoutName`
- `Workout.Details.WorkoutDescription`
- `Workout.Details.GoalDescription`
- `Workout.Details.Duration`
- `Workout.Details.TSS`
- `Workout.Details.IsOutside`
- `Workout.Details.Zones`
- `Workout.Details.ProfileName`
- `Workout.Details.Progression`
- `Workout.Details.ProgressionLevel`
- `Workout.Details.Levels`
- `Workout.IntervalData`
- `Workout.WorkoutData`

For target power, the current payload is more reliable through:

```text
Workout.WorkoutData[].Tick
Workout.WorkoutData[].FtpPercent
```

Sample tick values are one-second data points. When converting an interval:

1. Use `WorkoutData` values at the interval start and the final tick.
2. Fall back to `IntervalData[].StartTargetPowerPercent`.
3. Fall back to `EndTargetPowerPercent` when supplied.
4. Never silently invent power if no source value exists; emit a debug warning and 0 only as an
   explicit missing-data signal.

The `IntervalData[].Name == "Fake"` entries are recovery or gap segments used by the chart.
The mapper should still inspect them unless an application-specific rule explicitly excludes them.

## Sanitized example files

- [member-info.json](examples/member-info.json)
- [timeline.json](examples/timeline.json)
- [workout-details.json](examples/workout-details.json)
- [workout-search.json](examples/workout-search.json)

These files contain synthetic data and omit workout prose and private metadata.

## Capturing and updating examples

1. Open TrainerRoad in a private/incognito browser session.
2. Open Developer Tools and enable network log persistence.
3. Capture `/app/api/member-info`.
4. Open the calendar and capture `/app/api/react-calendar/{memberId}/timeline`.
5. Open one workout and capture `/app/api/workoutdetails/{workoutId}`.
6. Export or copy the relevant JSON.
7. Remove all cookies and authorization headers.
8. Replace member IDs, usernames, dates, URLs, descriptions, and workout IDs with synthetic values.
9. Compare response keys and types against [openapi.json](openapi.json).
10. Add or update a small regression fixture under `boot/src/test/resources/`.

If a response starts failing with 401/403/405/429:

- 401/403: verify the cookie is valid and belongs to the account.
- 405: check whether the HTTP method or route changed.
- 429: stop retrying and respect rate limits.
- 200 with unexpected JSON: compare top-level keys and types with the timeline/workout schemas.

Do not put raw HAR files or full response bodies into source control.
