# TESTING.md — Present — BDD Testing Plan

Cucumber + Espresso + Compose UI Test for instrumented BDD; JUnit for pure
domain logic. Read before writing any test.

---

## 1. Tool Stack

| Tool | Version | Purpose |
|------|---------|---------|
| Cucumber Android | 7.18.1 | BDD runner + Gherkin parser |
| Cucumber Gherkin Messages | 7.18.1 | Feature parser (ServiceLoader fix) |
| Espresso Core | 3.6.1 | System notification assertions |
| AndroidX Test Core/Runner | 1.6.1 | Instrumentation |
| AndroidX Test JUnit Ext | 1.2.1 | JUnit4 rules |
| Compose UI Test (junit4) | BOM-managed | Compose node assertions |

Runner: `io.cucumber.android.runner.CucumberAndroidJUnitRunner` (subclass as
`PingCucumberRunner` pattern — copies `.feature` assets to filesystem, sets
context classloader for ServiceLoader).

---

## 2. Directory Layout

```
app/src/androidTest/
├── assets/features/
│   ├── empty_state.feature
│   ├── subject_management.feature
│   ├── mark_attendance.feature
│   ├── undo_redo.feature
│   ├── edit_history.feature
│   ├── bulk_day_actions.feature
│   ├── calendar_view.feature
│   ├── today_schedule.feature
│   ├── bunk_calculator.feature
│   ├── timetable.feature
│   ├── notifications.feature
│   ├── auto_end_of_day.feature
│   ├── low_attendance_alerts.feature
│   ├── trend_detection.feature
│   ├── holiday_weekend_config.feature
│   ├── gamification.feature
│   ├── academic_session.feature
│   ├── cgpa_tracker.feature
│   ├── deadlines.feature
│   ├── focus_timer.feature
│   ├── study_materials.feature
│   ├── export_backup.feature
│   └── theme_switching.feature
├── java/com/aditya/present/androidtest/
│   ├── PresentCucumberRunner.kt
│   ├── hooks/DatabaseHooks.kt
│   └── steps/
│       ├── HomeSteps.kt, SubjectSteps.kt, AttendanceSteps.kt
│       ├── UndoRedoSteps.kt, EditHistorySteps.kt, BulkDaySteps.kt
│       ├── CalendarSteps.kt, TodaySteps.kt, TimetableSteps.kt
│       ├── BunkCalculatorSteps.kt, NotificationSteps.kt
│       ├── AutoMarkSteps.kt, LowAttendanceSteps.kt, TrendSteps.kt
│       ├── HolidayConfigSteps.kt, GamificationSteps.kt
│       ├── SessionSteps.kt, CgpaSteps.kt, DeadlineSteps.kt
│       ├── FocusTimerSteps.kt, StudyMaterialSteps.kt
│       ├── ExportSteps.kt, ThemeSteps.kt, NavigationSteps.kt
```

---

## 3. Runner Config

### `PresentCucumberRunner.kt`

```kotlin
class PresentCucumberRunner : CucumberAndroidJUnitRunner() {
    override fun onCreate(bundle: Bundle) {
        Thread.currentThread().contextClassLoader = javaClass.classLoader
        copyFeaturesToFs()
        super.onCreate(bundle)
    }
    // Copy .feature files from assets to filesDir for Cucumber to read
}
```

### `build.gradle.kts` (androidTest)

```kotlin
androidTestImplementation(libs.cucumber.android)
androidTestImplementation(libs.cucumber.gherkin.messages)
androidTestImplementation(libs.androidx.espresso.core)
androidTestImplementation(libs.androidx.test.runner)
androidTestImplementation(libs.androidx.test.rules)
androidTestImplementation(libs.androidx.test.junit.ext)
androidTestImplementation(libs.androidx.compose.ui.test.junit4)
```

### `DatabaseHooks.kt`

```kotlin
class DatabaseHooks {
    @Before fun clearDatabase() { /* clear all Room tables */ }
    @After fun cleanup() { /* close DB, clear Compose state */ }
}
```

---

## 4. Feature Scenarios

Each feature file contains 2–4 key scenarios. Full Gherkin is written during
implementation; below are the critical paths.

### `empty_state.feature`
- Empty dashboard shows "Add your first subject" CTA
- Tapping CTA opens AddEditSubjectScreen

### `subject_management.feature`
- Add subject with name, color, target %; acronym auto-generated
- Edit subject changes name and target
- Delete subject removes it and its attendance history

### `mark_attendance.feature`
- Mark class Present; percentage updates
- Mark class Absent; percentage drops
- Mark class Cancelled; excluded from percentage
- Mark class On Duty; tracked separately, excluded from %

### `undo_redo.feature`
- Undo a present mark via snackbar
- Redo an undone action
- Undo stack clears on screen change

### `edit_history.feature`
- Edit a past attendance entry from calendar
- Delete a past attendance entry

### `bulk_day_actions.feature`
- Mark all present for today
- Skip day marks all absent
- Mark day as holiday (excluded from %)

### `calendar_view.feature`
- Navigate months; color-coded days
- Tap a past day to see entries
- Streak indicator shows on calendar

### `bunk_calculator.feature`
- "You can bunk N more classes" when above target
- "Attend the next N to recover" when below target
- "Target no longer achievable" when beyond saving
- Scenario simulator: bunk 3 → projected % drops

### `notifications.feature` (@notifications)
- End-of-class notification with Mark Present/Absent buttons
- Pre-class reminder fires N minutes before
- Skip notification for already-marked classes

### `auto_end_of_day.feature`
- Unmarked classes auto-mark Present at 10 PM
- Auto-mark can be disabled
- Auto-marked entries can be edited

### `low_attendance_alerts.feature`
- Warning card when subject below target
- Push notification on threshold crossing
- At-risk badge on subject card
- Snooze alert for N days

### `trend_detection.feature`
- Trend warning when declining
- No warning when stable
- Combined below-target + declining message

### `holiday_weekend_config.feature`
- Set weekends (Sat+Sun); auto-excluded from calculations
- Add one-off holiday; all classes that day marked Holiday
- Holiday doesn't affect attendance %

### `gamification.feature`
- XP awarded for marking present
- Level up at XP threshold
- Perfect Week badge unlocked (7 days all present)
- Comeback Kid badge for recovery
- Badge gallery shows earned + locked

### `academic_session.feature`
- College student creates semester session (label "Semester")
- School student creates yearly session (label "Year")
- Onboarding asks for session type
- Yearly session hides CGPA tab
- Semester session shows CGPA tab
- Switching active session re-scopes dashboard

### `cgpa_tracker.feature`
- Add semester session with grades; SGPA + CGPA shown
- CGPA recalculates when new session added
- Target calculator shows needed SGPA
- US 4.0 grading scale
- CGPA tab hidden for yearly sessions

### `deadlines.feature`
- Add exam deadline with subject + due date; countdown shown
- Reminders fire at 7/3/1/0 days
- Swipe to mark done
- Exam clash with at-risk attendance highlighted

### `focus_timer.feature` (@focus)
- Start 25-min session; wavy progress + countdown shown
- Pause/resume
- Link session to subject; focus minutes added
- Today's focus minutes on home

### `study_materials.feature`
- Import PDF into subject; appears in list
- Open material in external viewer
- Delete material removes from list + storage
- Storage usage shown in settings

### `export_backup.feature`
- Export CSV; file contains attendance data
- Export JSON backup; contains all subjects
- Restore from backup; subjects restored

### `theme_switching.feature`
- Switch to AMOLED; background is pure black
- AMOLED disables dynamic color
- Switch to dark; background not black

---

## 5. Step Definition Plan

| Step class | Key matchers | Tools |
|------------|-------------|-------|
| `HomeSteps` | empty state, dashboard elements | `onNodeWithText`, `onNodeWithTag` |
| `SubjectSteps` | add/edit/delete subject, acronym auto-gen | `performTextInput`, seed via repo |
| `AttendanceSteps` | mark present/absent/cancelled/holiday/onduty | `onNodeWithTag`, `performClick` |
| `UndoRedoSteps` | snackbar UNDO/REDO | `onNodeWithTag("snackbar")` |
| `EditHistorySteps` | edit/delete past entry from calendar | seed via repo |
| `BulkDaySteps` | mark all present, skip day, mark holiday | `onNodeWithText` |
| `CalendarSteps` | month nav, color-coded days, streaks, swipe | `onNodeWithTag("calendarDay")` |
| `BunkCalculatorSteps` | safe bunks, recovery, beyond saving, simulator | `onNodeWithTag("bunkMeter")` |
| `NotificationSteps` | notification text, inline actions | Espresso `NotificationModule` |
| `AutoMarkSteps` | time reaches 10 PM, auto-marked, editable | fake clock, seed via repo |
| `LowAttendanceSteps` | warning card, notification, at-risk badge, snooze | `onNodeWithTag("lowAttendanceBanner")` |
| `TrendSteps` | trend warning, stable, combined warning | `onNodeWithTag("trendWarning")` |
| `HolidayConfigSteps` | set weekends, add holiday, auto-exclusion | seed via repo |
| `GamificationSteps` | XP, level, badges, confetti, gallery | `onNodeWithTag("badgeCard")`, `("xpHeader")` |
| `SessionSteps` | new session, type selection, label, CGPA tab visibility, switch active | seed via repo |
| `CgpaSteps` | grading system, SGPA, CGPA, target calc, yearly hides tab | `performTextInput` |
| `DeadlineSteps` | add deadline, countdown, reminders, swipe done, clash | `performSwipeRight` |
| `FocusTimerSteps` | start, pause, resume, wavy progress, subject link, summary | `onNodeWithTag("wavyProgress")`, fake clock |
| `StudyMaterialSteps` | import, open, delete, storage usage | file assertion via `File` |
| `ExportSteps` | export CSV/JSON, restore | `ApplicationProvider` + `File` |
| `ThemeSteps` | AMOLED black, dark, dynamic color disabled | `captureToImage` |
| `NavigationSteps` | tab nav, back nav | `onNodeWithTag` |

---

## 6. Unit Tests (JVM)

Pure `domain/` logic — no Android dependencies, fast.

| Class | Tests |
|-------|-------|
| `BunkCalculator` | safeBunks above target, recoveryNeeded below, isBeyondSaving unreachable, edge case: zero classes |
| `GpaCalculator` | sgpa UGC10 weighted, cgpa across sessions, target grades, US 4.0 mapping |
| `TrendAnalyzer` | isDeclining negative slope, stable false, percentage drop computation |
| `GamificationEngine` | awardXp, levelForXp thresholds, Perfect Week unlock, Comeback Kid unlock |

---

## 7. Tagging

| Tag | Scope | CI |
|-----|-------|----|
| `@smoke` | Core flows | Every PR |
| `@phase0` | v0.1 scaffold | Milestone |
| `@phase1` | Notifications, sessions, units, reschedule | Milestone |
| `@phase2` | Widgets, export, stats, charts, alerts, trends | Milestone |
| `@phase2b` | CGPA, deadlines, focus, materials, gamification | Milestone |
| `@phase3` | Geofence, AI scan, import | Milestone |
| `@notifications` | Needs notification permissions | Dedicated run |
| `@focus` | Needs foreground service + active timer | Dedicated run |
| `@geofence` | Needs location + emulator location | Dedicated run |
| `@wip` | Work in progress | Skipped in CI |

---

## 8. Conventions

- Seed state through repositories in `Given`; assert through UI in `Then`
- `waitForIdle()` after actions; no `Thread.sleep`
- Use `testTag` for structural nodes
- Fixed `Clock` in `domain/` for deterministic dates
- Notification tests use Espresso (Compose can't see system notifications)
- Clear DB between scenarios (`DatabaseHooks.@Before`)
- Scenarios are independent and deterministic
- Test both normal and edge cases (zero classes, beyond saving, empty state)
