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
