# AGENTS.md — Present — Technical Playbook

> **Read this before touching any code.** Single source of truth for
> architecture, conventions, permissions, and operational rules.

---

## 1. Overview

**Present** is a privacy-first Android attendance tracker for college and
school students. Kotlin + Jetpack Compose + Material 3 Expressive. 100% local
(Room DB), no backend, no cloud, no account. Single build with AdMob banner.

- **Package**: `com.aditya.present`
- **minSdk 26 / targetSdk 36** (Android 8.0–16)
- **No product flavors** — single `main` source set
- **No backend** — zero recurring cost; only network traffic is AdMob + optional
  user-supplied Gemini OCR key (stored locally in DataStore)

---

## 2. Feature Roadmap

### v0.1 — Scaffold
- Subject management (name, color, target %, acronym auto-gen)
- One-tap marking (Present/Absent/Cancelled/Holiday/On Duty) + undo/redo
- Edit/delete past entries, bulk day actions, daily attendance lock
- Swipe gestures, collapsible subject cards
- Calendar view (monthly, color-coded, streaks)
- Home dashboard (per-subject %, skip/attend guidance)
- Weekly timetable + Today's Schedule
- Safe Bunk & Recovery calculator
- Material 3 Expressive + AMOLED theme, glassmorphism, physics buttons
- Room persistence, AdMob banner, GitHub Actions CI

### Phase 1 — Core Tracking
- **1.1** Notifications with inline mark actions (P0) — AlarmManager + POST_NOTIFICATIONS
- **1.2** Academic sessions: semester (college) / yearly (school) (P1)
- **1.3** Multi-hour/unit tracking (P1)
- **1.4** Extra class & rescheduling with revert (P1)
- **1.5** Reboot recovery — BootReceiver re-schedules alarms (P0)
- **1.6** Auto end-of-day marking at 10 PM (P1)
- **1.7** Global weekend/holiday config (P1)

### Phase 2 — Intelligence
- **2.1** Bunk calculator + future scenario simulator (P0)
- **2.2** Streak tracking (P1)
- **2.3** Search & filter (P2)
- **2.4** Statistics + Vico trend charts (P2)
- **2.5** Low-attendance threshold alerts (P1)
- **2.6** Declining-trend detection (P2) — `domain/TrendAnalyzer.kt`

### Phase 2b — Academic Power
- **2.5** CGPA/GPA tracker (P1) — UGC 10-pt, US 4.0, UK; semester sessions only
- **2.6** Deadlines & exam reminders (P1) — 7/3/1/0-day alerts
- **2.7** Pomodoro focus timer (P2) — wavy progress, foreground service
- **2.8** Study materials (P2) — local PDFs/images via photo picker
- **2.9** Gamification (P2) — badges, XP, streak milestones; `domain/GamificationEngine.kt`

### Phase 3 — Convenience
- **3.1** Home screen widgets (P2)
- **3.2** CSV/JSON export & backup (P2) — formula-injection guarded
- **3.3** Timetable JSON import (P3)
- **3.4** Geofenced auto-attendance (P3 — opt-in, background location)
- **3.5** AI timetable scanner (P3 — user-supplied Gemini key)

### Phase 4 — Polish
- **4.1** Doze/battery optimization (P4)
- **4.2** Quiet hours (P3)
- **4.3** i18n (P3)
- **4.4** Onboarding flow (P2)

### Phase 5 — Future
- **5.1** Timetable sharing (P4)
- **5.2** Wear OS (P5)
- **5.3** Natural-language quick add (P5)

---

## 3. Architecture

```
app/src/main/java/com/aditya/present/
├── PresentApplication.kt          # AdMob init, theme bootstrap
├── MainActivity.kt                # Single-activity, Compose host
├── ui/
│   ├── theme/                     # Color, Theme, Type, Shape
│   ├── navigation/NavGraph.kt     # Routes: home, calendar, subject, timetable,
│   │                              #   cgpa, deadlines, focus, materials, settings
│   ├── screens/                   # HomeScreen, CalendarScreen, TodayScreen,
│   │                              #   SubjectScreen, AddEditSubjectScreen,
│   │                              #   TimetableScreen, BunkCalculatorScreen,
│   │                              #   CgpaScreen, AddEditSessionScreen,
│   │                              #   DeadlinesScreen, AddEditDeadlineScreen,
│   │                              #   FocusTimerScreen, StudyMaterialsScreen,
│   │                              #   SettingsScreen
│   └── components/                # SubjectCard, AttendanceChip, BunkMeterCard,
│                                  #   CalendarDay, CgpaCard, DeadlineCard,
│                                  #   FocusSummaryCard, WavyProgress, GlassSheet,
│                                  #   PhysicsButton, UndoSnackbar,
│                                  #   LowAttendanceBanner, TrendWarningCard,
│                                  #   BadgeCard, XpHeader, BannerAd
├── data/                          # Room entities, DAO, PresentDatabase,
│                                  #   PresentRepository, ThemeRepository
│                                  # Entities: Subject, ClassSlot, Attendance,
│                                  #   AcademicSession, Grade, Deadline,
│                                  #   FocusSession, StudyMaterial, Holiday,
│                                  #   Badge, Xp
├── domain/                        # Pure logic (no Compose imports)
│   # Enums: ThemeMode, SessionType, AttendanceStatus, GradingSystem,
│   #         DeadlineType, Badge
│   # Calculators: BunkCalculator, GpaCalculator, TrendAnalyzer,
│   #              GamificationEngine (all unit-tested)
├── service/                       # ClassAlarmReceiver, DeadlineAlarmReceiver,
│                                  #   AutoMarkReceiver, FocusTimerService,
│                                  #   BootReceiver, GeofenceService(P3)
└── util/                          # NotificationChannels, PermissionUtil,
                                   #   ExportUtil, FileUtil, AdConfig, LocationUtil
```

### State layers

| Layer | Where | Purpose |
|-------|-------|---------|
| UI | Compose screens | Stateless composables driven by ViewModel |
| ViewModel | `ViewModel` + `StateFlow<UiState>` | Calls repository |
| Repository | `PresentRepository` | Single CRUD + stats entry point |
| Persistence | Room (`present.db`) | All entities |
| Scheduling | `AlarmManager` + receivers | Class/deadline/auto-mark triggers |
| Focus | `FocusTimerService` (foreground) | Pomodoro + persistent notification |
| Theme | `DataStore<Preferences>` | Theme mode persistence |
| Ads | `BannerAd` composable | AdMob banner, test IDs in debug |

### Key formulas

**Attendance %**: `attendedUnits / totalUnits` where totalUnits = PRESENT + ABSENT
(CANCELLED/HOLIDAY/ON_DUTY excluded).

**Safe bunks**: `floor((attendedUnits / target) - totalUnits)` when above target.

**Recovery needed**: `ceil((target × totalUnits - attendedUnits) / (1 - target))`.

**Beyond saving**: `attendedUnits / totalUnits < target` AND no future classes
can lift it.

**CGPA (UGC 10-pt)**: `sgpa = Σ(gradePoint × credits) / Σ(credits)` per session;
`cgpa = Σ(sgpa × totalCredits) / Σ(totalCredits)` across sessions.

---

## 4. Theme & Motion Rules

- **ThemeMode enum**: `SYSTEM` / `LIGHT` / `DARK` / `AMOLED` (pure black surfaces)
- **AMOLED disables Dynamic Color** (can't produce true black)
- **Glassmorphism** (`GlassSheet`): `Modifier.blur()` + semi-transparent tint;
  falls back to opaque on API < 31
- **Physics buttons** (`PhysicsButton`): `withSpring` press + haptic
- **Wavy progress** (`WavyProgress`): custom `Canvas` on UI thread
- **Hero transitions**: `SharedTransitionLayout` (Compose 1.7+)
- **Staggered lists**: per-item delay = index × 40ms

> **NEVER** use JS-thread timers for continuous animation. All motion runs on
> the Compose UI thread via `Animatable` / `withSpring` /
> `SharedTransitionLayout`.

---

## 5. AdMob

- Test app ID: `ca-app-pub-3940256099942544~3347511713`
- Test banner: `ca-app-pub-3940256099942544/6300978111`
- `MobileAds.initialize()` in `PresentApplication.onCreate()`
- Banner on `HomeScreen` only — never on add/edit screens or Settings
- Replace test IDs before Play Store release

---

## 6. Operational Rules

### Commit rule
> **NEVER** append `Co-authored-by:`, `Authored-by:`, `Generated with`, or any
> bot trailer. Every commit authored solely by **Aditya Jain**
> (`jaditya700@gmail.com`). Do not change git identity.

### Build-test-commit
> **ALWAYS** build → test → commit per feature. No batching. Run
> `./gradlew assembleDebug` before every commit.

### Code style
- `camelCase` functions/variables, `PascalCase` classes/composables,
  `UPPER_SNAKE` constants
- No hardcoded user-facing strings — use string resources
- `StateFlow`/`Flow` for reactive data; never block main thread
- `service/` and `data/` free of Compose imports
- Pure logic in `domain/`, unit-tested
- Do NOT add/remove comments unless asked

---

## 7. Commands

```bash
./gradlew assembleDebug           # debug APK
./gradlew installDebug            # install on device
./gradlew test                    # unit tests
./gradlew connectedAndroidTests   # instrumented (Cucumber)
./gradlew bundleRelease           # release AAB
./gradlew lintDebug               # lint
```

---

## 8. Permissions & Android Compatibility

**minSdk 26 / targetSdk 36**

| Permission | Why | API |
|------------|-----|-----|
| `POST_NOTIFICATIONS` | Class/deadline/focus notifications | 33+ runtime, denied by default |
| `USE_EXACT_ALARM` | Exact alarm scheduling (granted on install) | 33+ manifest |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule alarms after reboot | all |
| `VIBRATE` / `WAKE_LOCK` | Notification vibration / CPU wake | all |
| `FOREGROUND_SERVICE` | FocusTimerService + GeofenceService(P3) | all |
| `FOREGROUND_SERVICE_SPECIAL_USE` | FocusTimerService type | 34+ mandatory |
| `FOREGROUND_SERVICE_LOCATION` | GeofenceService type (P3) | 34+ mandatory |
| `ACCESS_FINE/COARSE_LOCATION` | Geofenced auto-attendance (P3, opt-in) | 23+ |
| `ACCESS_BACKGROUND_LOCATION` | Auto-mark while closed (P3) | 29+ separate prompt |
| `INTERNET` / `ACCESS_NETWORK_STATE` | AdMob + optional Gemini OCR | all |

**No storage permission** — study materials use Photo Picker + SAF.

**Exact alarm strategy**: `USE_EXACT_ALARM` (granted on install, no prompt)
instead of `SCHEDULE_EXACT_ALARM` (denied by default on Android 13+).

**Foreground service types** (Android 14+): `specialUse` for Pomodoro,
`location` for geofence. Use `ServiceCompat.startForeground()` with type flags.

**Runtime permission flow** (first launch):
1. `POST_NOTIFICATIONS` (API 33+)
2. Exact alarms — `USE_EXACT_ALARM` auto-granted; fallback:
   `canScheduleExactAlarms()` → `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`
3. Location (P3 only, opt-in) — fine → background (separate prompt)
4. Study materials — no permission (photo picker)

---

## 9. Known Limitations (by design)

1. No backend, no cloud sync, no server — zero recurring cost
2. No account, no login, no analytics, no telemetry
3. Geofenced auto-attendance is opt-in (P3, background location)
4. AI scanner needs user-supplied Gemini key (stored locally)
5. AMOLED disables Dynamic Color
6. CSV export is formula-injection guarded (RFC 4180)
7. No `READ_EXTERNAL_STORAGE` — photo picker + SAF only

---

## 10. Testing

Full BDD strategy in **[TESTING.md](TESTING.md)**. Read before writing tests.

- **Stack**: Cucumber 7.18.1 + Espresso 3.6.1 + Compose UI Test
- **Unit tests**: `domain/` calculators (Bunk, GPA, Trend, Gamification)
- **Instrumented**: `.feature` files + step classes via `CucumberAndroidJUnitRunner`
- **Tags**: `@smoke`, `@phase0..3`, `@phase2b`, `@notifications`, `@focus`, `@geofence`

---

## 11. Research Basis

Consolidated from GitHub open-source attendance apps (AttendanceHub,
Self-Attendance, Attendo, Markd, AttendMate, AttendX, AttendEase), commercial
apps (Schedo, Bunk Planner), Reddit student threads (r/androiddev, r/College,
r/EngineeringStudents), and Material 3 Expressive guidance.
