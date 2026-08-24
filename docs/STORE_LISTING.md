# Play Store Listing — Aviation Exam Pro

Copy-paste ready. Replace the current listing text field by field.

## App title (30 char limit)

```
Aviation Exam Pro: KCAA Prep
```
28 chars. The title is the single strongest ranking signal on Play — "KCAA" and
"Prep" are the terms candidates actually type. Do not use the bare app name.

Alternatives if you want to widen the net later:
- `Pilot Exam Prep: KCAA & ICAO` (28)
- `ATPL CPL Exam Prep + Logbook` (28)

## Short description (80 char limit)

```
7,900+ KCAA & ICAO pilot exam questions with a free digital logbook. Offline.
```
76 chars. Indexed for search, and it's the first line users read. The current
one ("Professional aviation exam preparation with offline support and progress
tracking") contains none of the words anyone searches for.

## Full description (4000 char limit)

```
Pass your KCAA pilot exams with 7,900+ practice questions — free, and fully offline.

Aviation Exam Pro is built for student pilots and CPL/ATPL candidates preparing
for Kenya Civil Aviation Authority and ICAO-aligned written exams. Practise on
real exam-format multiple choice questions, track your weak subjects, and log
your flight hours — all in one app that works without an internet connection.

WHAT YOU GET

• 7,900+ exam questions across all 7 subjects
• Unlimited mock exams — no daily limits, no locked sections
• Instant results with explanations for every question
• Full offline access — study on the apron, in the air, anywhere
• Digital pilot logbook with PDF export for your instructor or examiner
• Progress tracking across every subject
• Free to use

SUBJECTS COVERED

• Air Law
• Meteorology
• Principles of Flight
• Aircraft General Knowledge
• Human Performance and Limitations
• Operational Procedures
• Navigation

BUILT-IN PILOT LOGBOOK

Record flights as you fly them: date, aircraft type and registration, route,
PIC, dual, night, IFR and cross-country time. Export a clean, regulation-format
PDF whenever your instructor, school, or examiner asks for it.

STUDY OFFLINE, ANYWHERE

Every question is stored on your device. No data, no signal, no problem —
useful at airfields and training rooms where coverage is unreliable.

WHO IT'S FOR

Student pilots, PPL candidates, CPL and ATPL candidates, flight school students,
and anyone preparing for KCAA or ICAO-aligned aviation theory exams.

Questions, corrections, or a subject you'd like added? Send feedback from inside
the app — we read everything.
```

## Category and tags

- **Category:** Education
- **Tags:** Education → Test Prep (add "Books & Reference" as secondary if offered)

## Keywords to keep present in the listing text

Play indexes the title, short description, and full description. These should
appear naturally at least once: KCAA, pilot exam, aviation exam, ATPL, CPL, PPL,
student pilot, air law, meteorology, navigation, principles of flight, logbook,
offline, Kenya, ICAO.

## Screenshots (highest-impact asset — 8 slots)

Play conversion is driven by the first two screenshots more than by any text.
Put a one-line caption on each image; don't ship bare screen captures.

1. Exam question in progress — caption: "7,900+ real exam-format questions"
2. Results screen with a pass — caption: "Instant scoring and explanations"
3. Subject grid — caption: "All 7 KCAA subjects covered"
4. Logbook list — caption: "Digital logbook built in"
5. PDF export — caption: "Export a regulation-format PDF"
6. Offline state — caption: "Works with no internet"
7. Progress/stats — caption: "See your weak subjects"
8. Free callout — caption: "Free — no exam limits"

## Feature graphic (1024x500)

Aircraft cockpit or horizon background, app name, and the single line
"7,900+ KCAA Exam Questions — Free & Offline".

## Release notes — v1.2.0 (versionCode 9)

The pilot logbook is BUILT BUT HIDDEN in this build
(`FeatureFlags.logbookEnabled = false`), so nothing below mentions it. Do not
re-add logbook lines until that flag is turned on.

Play's "What's new" allows 500 characters. Counts verified.

### Option A — bullets with a lead line (279 chars) — RECOMMENDED

```
Every subject is now unlocked — unlimited free practice with no exam limits.

• Share your exam results with study partners and instructors
• Report a problem question directly from the exam screen
• Updated for the latest Android release
• Performance and stability improvements
```

### Option B — pure bullets (243 chars)

```
• Unlimited free practice — every subject unlocked, no exam limits
• Share your exam results with study partners
• Report a problem question from the exam screen
• Updated for the latest Android release
• Stability and performance improvements
```

### Option C — prose (264 chars)

```
Unlimited free practice — every subject unlocked, no exam limits.

Share your results with study partners, and report a problem question straight from the exam screen.

Updated for the latest Android release, with stability and performance improvements throughout.
```

### Before you paste

The repository has a single commit at versionCode 1, so it cannot confirm what
actually shipped in the live 1.1.1 build. If unlimited free practice or question
reporting was ALREADY live, delete those lines — re-announcing an existing
feature as new reads as padding and gives users nothing to come back for. If both
were already live, use Option D:

```
• Updated for the latest Android release
• Stability and performance improvements
```

Do not translate manually; Play auto-translates listings for enabled locales.

## Internal changelog — not for Play

**Compliance (the reason for this release)**
- Google Play Billing 7.0.0 → 9.1.0 (required by 31 Aug 2026)
- targetSdk/compileSdk 35 → 36 (required by 31 Aug 2026)
- Toolchain: Kotlin 2.0.0 → 2.2.10, AGP 8.7.1 → 8.13.2, Gradle 8.9 → 8.14.3,
  Room 2.6.1 → 2.8.4

**User-visible**
- Paywall off (`subscriptionEnabled = false`) — exams unlimited and free
- Share exam result / share app
- Play in-app review prompt after 3 exams on a pass

**Built but hidden behind `logbookEnabled = false`**
- Block/air/ground times, landings, instrument approaches (Room migration 5 → 6,
  which still runs on upgrade even though the UI is hidden)
- PDF export columns, FileProvider, unified logbook identity
- Nav fixes: `flight_entry/{flightId}` route, `loadFlight()` implementation

**Known defects blocking the logbook**
- `LogbookViewModel.loadFlights()` sets `isLoading = true` but only the Room Flow
  collector clears it; returning from the entry screen without saving produces no
  emission, so the spinner never stops
- `ProfileViewModel` is a stub, so the PDF header has no licence number
