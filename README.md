# dhun

terminal-themed offline music player for android.

black background · single green accent · monospace everywhere · square corners · no rounded anything.

## features (v0.1.0 — offline only)

- scans local device audio via `MediaStore`
- library: songs / albums / artists / folders tabs
- persistent mini-player above bottom tab bar
- full-screen now playing with square transport, waveform-style progress bar, queue footer
- queue sheet (reorder/remove stubs), playlists (user-creatable + smart auto lists)
- search with recent searches
- settings: library scan, crossfade, eq presets, theme/accent swatches, sleep timer
- zero network permission

`src: online · online coming soon` slot reserved in library header for future online/yt source support.

## building (ubuntu cli)

prerequisites you already have installed:

- jdk 17 (`sudo apt install openjdk-17-jdk-headless`)
- android sdk with cmdline-tools, platform 34, build-tools 34.0.0
- `ANDROID_HOME` exported (e.g. `~/Android/Sdk`)
- gradle (wrapper jar generated below)

### 1. generate gradle wrapper jar

repo ships with `gradle-wrapper.properties` pointing to gradle 8.7. generate the wrapper jar the first time:

```bash
# if you have gradle installed globally:
gradle wrapper --gradle-version 8.7

# or, if only sdkmanager/java, download wrapper jar manually:
mkdir -p gradle/wrapper
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar \
  -o gradle/wrapper/gradle-wrapper.jar
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradlew -o gradlew
chmod +x gradlew
```

### 2. tell gradle where the sdk is

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 3. build debug apk

```bash
./gradlew assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

### 4. install to a connected device / emulator

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## project layout

```
app/src/main/java/com/dhun/app
├── DhunApp.kt                 - application singleton (repo + player + prefs)
├── data/
│   ├── Models.kt              - Song, Album, Artist, Folder, Playlist
│   ├── LibraryRepository.kt   - MediaStore scan, in-memory library
│   └── Prefs.kt               - DataStore-backed settings
├── player/
│   ├── DhunPlayer.kt          - ExoPlayer wrapper, queue/shuffle/repeat state
│   └── DhunPlaybackService.kt - Media3 MediaSessionService
├── ui/
│   ├── MainActivity.kt        - NavHost + tabs + mini-player host + queue sheet
│   ├── theme/                 - terminal colors, monospace typo, square shapes
│   ├── components/            - TIcon, TermButton, SongRow, MiniPlayer, tabs...
│   └── screens/
│       ├── splash/            - boot + scanning progress
│       ├── permission/        - storage access prompt
│       ├── library/           - songs/albums/artists/folders + sort sheet
│       ├── album/             - album / artist / folder detail
│       ├── nowplaying/        - full-screen player
│       ├── queue/             - queue bottom sheet
│       ├── playlists/         - playlists list + detail
│       ├── search/            - search + recent
│       └── settings/          - all settings + eq / appearance / folders / sleep / about
```

## roadmap

- [ ] drag-to-reorder in queue (stubbed with handle icon)
- [ ] swipe-to-remove in queues/playlists
- [ ] embedded album art via coil (placeholders currently render `♫`)
- [ ] per-track action sheet (add to playlist / queue / ringtone / share / delete)
- [ ] waveform rendering (current progress is a bar)
- [ ] equalizer actually wired to ExoPlayer `Equalizer` audio processor
- [ ] sleep timer real scheduling
- [ ] playlist persistence to disk (currently in-memory)
- [ ] online sources slot (youtube/streaming) behind the `[src: online]` badge
