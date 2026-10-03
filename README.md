# Warrior

**Warrior** is an offline-first Android tracker for **boxing and strength** training, built Persian-first
(RTL, Jalali calendar, Persian digits) with an English locale alongside.

No account. No login. No `INTERNET` permission. Your training data never leaves the device except
through a manual export that you initiate.

---

## Status

| Phase | Scope | State |
|---|---|---|
| **0** | Project foundation: Gradle KTS, Compose/M3, Hilt, Room (10 tables), dark/light theme, FA/EN + RTL + Jalali, CI, unit tests | ✅ complete (audited & fixed — see below) |
| **1A** | Workouts: integrated bodyweight Strength exercises, resumable draft, reps/timed sets, recent-session tracking | ✅ complete in v0.2.0 |
| 1B | Weighted strength input, timer + foreground service | ⬜ |
| 2 | Boxing activities, round/rest timer, manual rounds | ⬜ |
| 3 | History (list + Jalali calendar), detail, edit, delete with undo | ⬜ |
| 4 | Progress: volume / PR / e1RM, stats cache, charts, goals, body weight | ⬜ |
| 5 | Exercise library seed, JSON backup/restore, migration tests | ⬜ |
| 6 | Accessibility, performance, release signing | ⬜ |

See [`PHASES.md`](PHASES.md) for the phase plan and delivery reports, and
[`ARCHITECTURE.md`](ARCHITECTURE.md) for the full design document (18 sections — domain model,
schema, metric definitions, validation rules, testing strategy).

### Bodyweight Strength in Workouts (v0.2.0)

Bodyweight is integrated into Warrior's existing Workouts flow as a Strength load type—not a
separate program or section. Start or resume the normal workout draft, add one of 19 bilingual
bodyweight exercises from the Strength picker, log repetition or timed-hold sets, remove mistakes,
finish the session, and review recent workout totals. Every confirmed set is written to Room
immediately; finishing refreshes affected daily-stat cache rows from the raw sets. The logging
models and repository boundary remain generic so weighted and assisted Strength inputs can be
added without creating another workout flow.

---

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 1.9.24, Coroutines + Flow |
| UI | Jetpack Compose + Material 3 (dark-first), Navigation Compose |
| State | ViewModel + `StateFlow`, one `UiState` per screen |
| DI | Hilt 2.51 |
| Database | Room 2.6 + KSP, `exportSchema = true`, schemas committed under `docs/schemas/` |
| Preferences | DataStore (the only settings store — there is no `settings` table) |
| Serialization | kotlinx.serialization (backup JSON + library seed) |
| Time | `java.time` plus a pure-Kotlin Jalali engine (no `android.icu`, so results are identical on every device) |
| Build | Gradle 8.7, AGP 8.5.2, JDK 17, `minSdk 26` / `targetSdk 34` |
| Test | JUnit 4, Turbine, kotlinx-coroutines-test |

---

## Repository layout

```
android-app/src/main/java/com/warrior/tracker/
├── MainActivity.kt              Single-Activity host (AppCompatActivity — required for per-app language)
├── MainViewModel.kt             App-wide UI state (theme mode)
├── core/
│   ├── common/                  Domain enums, WarriorResult/WarriorError, safe enum parsing
│   ├── navigation/              Screen routes + bottom-nav shell
│   ├── settings/                DataStore-backed SettingsRepository + ResolvedSettings
│   ├── time/                    Clock abstraction, Jalali engine, NumberInputParser
│   ├── ui/theme/                Material 3 dark/light palettes
│   └── validation/              InputValidator — the sec.15 range table, single source of truth
├── data/local/
│   ├── converter/               Room enum <-> TEXT TypeConverters
│   ├── dao/                     8 DAOs
│   ├── database/                AppDatabase (10 entities, version 1)
│   └── entity/                  Room entities
├── di/                          Hilt modules (database, DAOs, dispatchers, app scope, clock)
├── domain/calculator/           Pure metric functions: Volume, Intensity, PR/e1RM, Goal, WeeklySummary
└── feature/                     home, settings, history, progress, workout

docs/schemas/                    Room schema JSON — tracked in Git, feeds MigrationTestHelper (Phase 5)
```

Layer rule (sec.4.1): `feature → domain → data`. `core` is shared. Domain calculators are pure
Kotlin with no Android imports, so they are unit-testable without a device or Robolectric.

---

## Data model

Ten tables, all keyed by UUID so backup merge and future sync stay possible:

`workouts` → `activities` → `sets` / `rounds`, plus `exercises`, `exercise_muscles`, `goals`,
`body_weights`, and two **derived, rebuildable caches**: `exercise_daily_stats` and `pr_events`.

Design decisions worth knowing before you touch the schema (sec.10.1):

- **Workout is the centre of the system**, and one workout may mix strength and boxing.
- **Activity is the only middle layer** — no other table sits between it and sets/rounds.
- **Nothing derived is stored**: workout tonnage, activity duration/intensity, goal `current`,
  progress charts and the calendar are all computed. The two cache tables exist purely for
  performance and can be dropped and rebuilt from `sets`/`rounds` at any time.
- **Times are UTC epoch millis**; `local_date` (ISO) and `timezone_id` are stored alongside so
  history and the calendar survive a timezone change.
- **Weight is always kg**; unit conversion happens in the display layer only.
- **Exercises are never hard-deleted** — they are archived, so history keeps resolving.
- `fallbackToDestructiveMigration` is forbidden (sec.12). Every schema change gets an explicit
  migration and a committed schema JSON.

---

## Metrics

All metric definitions live in `domain/calculator/` as pure functions and are covered by unit tests.

- **Tonnage** = `SUM(external_load_kg × reps)`; `BODYWEIGHT` and `ASSISTED` contribute 0.
- **Effective volume** (secondary, `BODYWEIGHT_PLUS` only) uses the body-weight snapshot, and
  reports *nothing* rather than 0 or a guess when no snapshot exists.
- **e1RM** = Epley, `w × (1 + reps/30)`, exactly `w` at one rep, only meaningful for 1..12 reps.
- **PR** — five types (`MAX_WEIGHT`, `REPS_AT_WEIGHT`, `BEST_E1RM`, `BEST_SET_VOLUME`,
  `MAX_DURATION`) so that `80 kg × 12` and `80 kg × 1` are not the same record. Ties are **not**
  new records; improvement must be strict.
- **Intensity** — round intensity 1..10 (optional); activity intensity is the duration-weighted
  mean and is never stored.
- **Goal progress** = `clamp((current − start) / (target − start), 0, 1)`; direction is inferred
  from `start` vs `target`, so weight-loss goals work without a separate flag. `current` is never
  stored.

Only **completed, non-warm-up sets of completed workouts** are counted (sec.7).

---

## Localization

- Persian (RTL) and English (LTR) from day one; all layouts use `Start`/`End`, never `Left`/`Right`.
- **Per-app language** via `AppCompatDelegate.setApplicationLocales`, advertised to the platform
  through `res/xml/locales_config.xml`. `MainActivity` extends `AppCompatActivity` because the
  AppCompat locale override is only honoured by AppCompat activities on API 32 and below.
- **Jalali calendar** for display; storage stays Gregorian. The engine is anchor-table exact for
  AP 1340–1450 (1961–2071 CE) and falls back to the official Iranian 33-year sub-cycle algorithm
  outside it, so it never produces an impossible month or day for any realistic device clock.
- **Persian digits** are a display setting. `NumberInputParser` accepts Latin, Persian and
  Arabic-Indic digits plus `٫`/`،`/`.` decimal separators on input, so a Persian keyboard never
  yields "invalid number". All digit formatting is locale-pinned to `Locale.ROOT`, because
  `String.format("%d", …)` is localised by default and would emit Arabic-Indic glyphs on an `ar`
  device.
- Week start is configurable and defaults to **Saturday for Persian, Monday for English**.

---

## Building

Requirements: JDK 17 and the Android SDK with platform 34 / build-tools 34.0.0.

```bash
echo "sdk.dir=/path/to/android-sdk" > local.properties   # or set ANDROID_HOME

./gradlew :android-app:testDebugUnitTest     # unit tests
./gradlew :android-app:lintDebug             # lint report -> android-app/build/reports/
./gradlew :android-app:assembleDebug         # APK -> android-app/build/outputs/apk/debug/
```

CI (`.github/workflows/build.yml`) runs those tasks on every push and PR, then verifies that the
Room schema JSON under `docs/schemas/` is present and not stale, and publishes the unit test
results, the lint report and the debug APK as artifacts.

`NewApi`, `MissingTranslation`, `ExtraTranslation`, `FullBackupContent` and `RtlHardcoded` are
promoted to lint **errors** because they matter for a minSdk-26, offline, bilingual RTL app.
Lint still runs with `abortOnError = false` until a baseline is committed in Phase 6, so its report
is published as an artifact rather than gating the build — but it already earned its place: it is
what caught `LocalDate.ofInstant` (API 34 only) in `Clock.localDateToday`, which would have thrown
`NoSuchMethodError` on every device below Android 14.

> `gradle.properties` asks for a 2 GB Gradle heap. AGP 8.5 + KSP + Hilt + the Compose compiler
> genuinely need it — the Phase-0 setting of 1 GB with an in-process Kotlin compiler crashed the
> daemon with an out-of-memory error during `:app:mergeExtDexDebug`.

---

## Privacy

- No `INTERNET` permission is declared in the manifest.
- No analytics, no crash reporting, no account, no third-party SDK.
- Android Auto Backup is enabled as a *second* layer (Room database, DataStore settings and the
  AppCompat locale store). The authoritative backup is the manual JSON export arriving in Phase 5.
- Exported backup files are **not encrypted**; the app will remind the user of this on export.

---

## License

To be decided.
