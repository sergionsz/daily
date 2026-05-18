# Contributing

Thanks for your interest in contributing to **daily**, a small Android app for daily tasks and habits.

## Project setup

Requirements:
- JDK 17
- Android SDK (compileSdk 36, minSdk 24)
- Android Studio (recommended) or any IDE with Kotlin and Gradle support

Clone and open the project in Android Studio, or build from the command line:

```sh
./gradlew assembleDebug
```

## Development workflow

1. Fork the repo (external contributors) or create a branch (collaborators).
2. Use a descriptive branch name, e.g. `feature/calendar-filter` or `fix/calendar-back-handler`.
3. Make focused commits with clear messages.
4. Open a pull request against `main`. Include a short description of what changed and why.

## Code style

- The app is written in Kotlin with Jetpack Compose (Material 3).
- Follow the conventions already in the codebase: small composables, state hoisted to the screen, `remember` / `rememberSaveable` for local UI state.
- Keep functions short and prefer descriptive names over comments. Add a comment only when the *why* is non-obvious.
- New screens should match the existing visual language (rounded surfaces, primary/primaryContainer tokens, `MaterialTheme.typography`).

## Building and testing

Common commands:

```sh
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (requires signing env vars; see below)
./gradlew test                   # JVM unit tests
./gradlew connectedAndroidTest   # instrumentation tests (requires a device/emulator)
```

Please run the relevant test commands before opening a PR. If you change UI, mention how you verified it (device, emulator, or screenshots).

## Release signing

Release builds use a keystore provided via environment variables:

- `KEYSTORE_PATH` — path to the `.jks` file
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

If `KEYSTORE_PATH` is unset, release builds fall back to the debug signing config (useful for local smoke tests).

## Releases

Releases are cut by pushing a `v*` tag:

```sh
git tag v0.2.0
git push origin v0.2.0
```

The `Build APK` GitHub Actions workflow builds the release APK, attaches it to a generated GitHub Release, and auto-fills release notes from commits since the previous tag. Pushes to `main` build the APK as a workflow artifact only; they do not create a release.

## Reporting issues

Please include:
- Device and Android version
- Steps to reproduce
- What you expected vs. what happened
- A logcat snippet or screenshot if relevant
