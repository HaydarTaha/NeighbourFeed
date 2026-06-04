# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

This is a standard Android Gradle project. Build and run via Android Studio or the Gradle wrapper:

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run unit tests
./gradlew test
```

There is no local dev server — the app must be deployed to an Android device or emulator (min SDK 23 / Android 6.0).

## Architecture

Single-module Android app written in Java. No architectural pattern (MVVM, MVP, etc.) is applied — all logic lives directly in Activity classes.

**Navigation flow:** `Login` → `Register` or `MainActivity` → `CreatePost` / `PostPage` / `CommentPage` / `UserProfile`

**Data layer:** Direct Firebase calls from Activities and Adapters — no repository or service layer. Three Firestore collections:
- `Posts` — post documents with fields: `content`, `createDate`, `location` (GeoPoint), `mediaPath`, `mediaType`, `type`, `upVotedUsers[]`, `downVotedUsers[]`, `userName`
- `Comments` — document per postId with a `comments[]` array
- User data managed via Firebase Auth only (no separate Users collection)

**Location:** `MainActivity.findUserLocation()` uses `LocationManager` with GPS_PROVIDER. All buttons are disabled until location resolves. Distance filtering uses the Haversine formula in `calculateDistanceFromUser()`.

**Media:** Images captured via camera intent or picked from gallery, stored in Firebase Storage. Audio recorded via `MediaRecorder`, also stored in Firebase Storage. File URIs shared via `FileProvider` (authority: `com.neighbourfeed.fileprovider`).

**Post list rendering:** `PostAdapter extends ArrayAdapter<Post>` inflates `activity_post.xml` per item. Vote updates write directly to Firestore from the adapter. `Post` implements `Parcelable` for passing between activities via Intent.

## Key Constraints

- `google-services.json` must be present in `app/` for Firebase to initialize — this file is gitignored and must be obtained separately.
- Location permission (`ACCESS_FINE_LOCATION`) is required at runtime before any posts load.
- The `ListView` height in `activity_main.xml` is hardcoded to `632dp` — avoid this pattern when adding new layouts.
- `Post`'s `Parcelable` implementation does not serialize `postDate`, `latitude`, or `longitude` — these are passed separately as Intent extras in `PostAdapter.onClickPost()`.
