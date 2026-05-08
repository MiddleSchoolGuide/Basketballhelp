# Hoop Dev Android

Native Android version of the Hoop Dev player development tracker, built with Kotlin, Jetpack Compose, MVVM, Coroutines/Flow, Navigation Compose, and Retrofit.

## Structure

- `app/src/main/java/com/example/basketballhelp/data`
  Network DTOs, Retrofit service, repository implementations, and backend integration utilities.
- `app/src/main/java/com/example/basketballhelp/domain`
  Core models, repository contracts, analytics, validation, and recommendations.
- `app/src/main/java/com/example/basketballhelp/ui`
  Navigation shell, screen viewmodels, feature screens, shared Compose components, and theme.

## Included features

- Dashboard with backend-backed player/session/goal data, development score, charts, recommendations, notes, and shooting summary
- 5-step native session logging flow with validation and API persistence
- History list with expandable details, edit flow, and delete confirmation
- Daily drill checklist grouped by category with backend completion toggles
- Profile screen with editable player info, averages, baseline-to-target progress, and development score improvement

## Run

1. Open the project in Android Studio.
2. Sync Gradle and run the `app` configuration on an emulator or device.

## Backend configuration

The Android app uses the same HTTP API contract as the web app in `C:\Users\sidep\WebstormProjects\basketball_progress`.

Set the backend base URL with a Gradle property named `HOOP_DEV_BASE_URL`. The value must include the trailing slash.

Examples:

- Local Next.js dev server: `http://10.0.2.2:3000/`
- Deployed backend: `https://your-deployed-host/`

You can set it in one of these places:

- `~/.gradle/gradle.properties`
- project `gradle.properties`
- Android Studio Gradle VM/property configuration

Example:

```properties
HOOP_DEV_BASE_URL=http://10.0.2.2:3000/
```

If no property is set, the app defaults to `http://10.0.2.2:3000/`.

## API contract mirrored

The Android client mirrors these web routes:

- `GET /api/sessions?playerId=1`
- `GET /api/sessions?playerId=1&limit=n`
- `POST /api/sessions`
- `GET /api/sessions/{id}`
- `PUT /api/sessions/{id}`
- `DELETE /api/sessions/{id}`
- `GET /api/goals?playerId=1`
- `POST /api/goals`
- `GET /api/drills?date=yyyy-MM-dd`
- `POST /api/drills`
- `GET /api/players`
- `POST /api/players`
- `GET /api/players/{id}`
- `PUT /api/players/{id}`

## Notes

- The active repositories are network-backed through Retrofit with logging and request timeouts.
- Room and its seeded local-first schema have been removed from the runtime path for this app.
- Screen calculations and recommendation logic remain in the Android domain layer.
- Gradle output is redirected to `out/app-build` because this environment had a permission issue under the default module-local `app/build` path.
