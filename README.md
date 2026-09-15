# Present

> **Every class counts.**

A privacy-first attendance tracker for college and school students. Track
attendance with one tap, see exactly how many classes you can safely skip,
manage your timetable, calculate your CGPA, never miss a deadline, and run
focus sessions — all offline, no account needed.

Built with **Kotlin + Jetpack Compose + Material 3 Expressive**.

## Features

**Attendance & calendar**
- One-tap marking: Present / Absent / Cancelled / Holiday / On Duty
- Undo/redo, edit/delete past entries, bulk day actions, swipe gestures
- Monthly calendar with color-coded days, streaks, and day detail
- Auto end-of-day marking (unmarked classes auto-mark Present at 10 PM)
- Daily attendance lock (prevents duplicate entries)

**Smart guidance**
- Safe Bunk & Recovery calculator with "beyond saving" warning
- Future scenario simulator ("what if I bunk the next 3?")
- Low-attendance threshold alerts + declining-trend detection
- Vico trend charts (weekly/monthly)

**Academic tools**
- Academic sessions: semester (college) or yearly (school)
- CGPA/GPA tracker: India 10-point, US 4.0, UK (semester sessions only)
- Deadlines & exam reminders (7/3/1/day-of notifications)
- Pomodoro focus timer with wavy progress indicator
- Local study materials (PDFs, images — no cloud, photo picker)
- Gamification: badges, XP, streak milestones (local-only)

**Timetable & scheduling**
- Weekly timetable with Today's Schedule
- Multi-hour/unit tracking (2-hr lab = 2 units)
- Extra class & rescheduling with revert
- Global weekend/holiday config
- Notifications with inline mark actions + pre-class reminders

**Design & privacy**
- Material 3 Expressive: glassmorphism, physics buttons, hero transitions
- System / Light / Dark / AMOLED themes; Dynamic Color on Android 12+
- 100% local: Room DB, no backend, no cloud, no account, no tracking
- Ad-supported: single AdMob banner on Home (never mid-task)
- Offline-first; minSdk 26, targetSdk 36 (Android 8.0–16)

## Tech Stack

- Kotlin 2.0, Jetpack Compose, Material 3 Expressive
- Single-Activity MVVM + Clean Architecture (Repository pattern)
- Room + KSP + Flow | DataStore | Hilt | Navigation Compose
- WorkManager + AlarmManager | Vico charting
- Cucumber 7.18.1 + Espresso 3.6.1 + Compose UI Test

## Getting Started

> The Gradle/Compose scaffold is not yet generated. This README + AGENTS.md
> define the full plan; the Android project will be bootstrapped in the first
> feature commit.

```bash
./gradlew assembleDebug          # debug APK
./gradlew test                   # unit tests
./gradlew connectedAndroidTests  # instrumented tests (Cucumber)
```

## App Icon

**Concept**: A bold checkmark forming the letter "P", inside a rounded
square with a Material 3 teal-to-blue gradient. Monochrome variant for
Android 13+ themed icons.

**Generate one for free**:
- [AppIconKit](https://appiconkit.com/) — AI-generated from a text prompt,
  exports all Android density buckets + adaptive icon XML + Play Store 512px
- [CloudIcon Studio](https://cloudicon.typely.in/) — upload artwork, get
  mipmaps + adaptive icons + monochrome + Play Store icon
- Android Studio → `res` → New → Image Asset → Launcher Icon (built-in)

Prompt suggestion for AI generators:
> "Minimal app icon for an attendance tracker called Present. A white
> checkmark forming the letter P, inside a rounded square with a teal-to-blue
> Material 3 gradient. Clean, flat, no text, centered, adaptive icon safe
> zone."

## License

To be decided (likely GPL-3.0).
