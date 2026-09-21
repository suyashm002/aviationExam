# Play Store Listing — Aviation Exam Pro

Copy-paste ready. Replace the current listing text field by field.

**The pilot logbook is hidden in 1.2.1 (`FeatureFlags.logbookEnabled = false`).**
Nothing in the public copy below mentions it; re-add the logbook lines only
when the flag is turned on.

## App title (30 char limit)

```
Aviation Exam Prep: PPL Pilot
```
29 chars. Tuned 2026-09-21 for the searches "aviation test", "aviation
preparation", "pilot training" and "PPL exam": the title is the strongest
ranking signal on Play, and it now carries *aviation*, *exam*, *prep*, *PPL*
and *pilot*. "KCAA" moved into the short and full descriptions, which are also
indexed. Do not use the bare app name.

Alternatives if the rank for one term matters more than the others:
- `Pilot Exam Prep: PPL & Aviation` (31 — one too long; drop the "&")
- `PPL Exam Prep: Pilot Training` (29 — strongest for "pilot training")
- `Aviation Exam Prep: KCAA Prep` (28 — the previous, Kenya-first title)

## Short description (80 char limit)

```
Pilot training test prep: 7,900+ PPL, CPL & ATPL aviation exam questions offline
```
80 chars exactly. Indexed for search and the first line users read. Carries
*pilot training*, *test prep*, *PPL*, *CPL*, *ATPL*, *aviation exam*, *offline*.

## Full description (4000 char limit)

```
Pass your PPL, CPL or ATPL aviation exam with 7,900+ practice questions — free, fully explained, and fully offline.

Aviation Exam Prep is pilot training test preparation built for student pilots and CPL/ATPL candidates preparing for KCAA and ICAO-aligned written exams. Practise on real exam-format multiple choice questions, read a written explanation for every answer, and track your weak subjects — all in one app that works without an internet connection.

WHAT YOU GET

• 7,900+ aviation exam questions across all 7 pilot theory subjects
• A written explanation for every single question — learn why, not just what
• Unlimited mock exams — no daily limits, no locked sections
• Instant results and subject-by-subject progress tracking
• Full offline access — study on the apron, in the air, anywhere
• Free to use

SUBJECTS COVERED

• Air Law
• Meteorology
• Principles of Flight
• Aircraft General Knowledge
• Human Performance and Limitations
• Operational Procedures
• Navigation

AVIATION TEST PREPARATION THAT EXPLAINS ITSELF

Most pilot exam apps show you the key and move on. Every question here comes with a clear explanation of the underlying rule, formula or procedure, written for the PPL and CPL syllabus, so a wrong answer becomes a lesson instead of a guess.

STUDY OFFLINE, ANYWHERE

Every question is stored on your device. No data, no signal, no problem — useful at airfields and training rooms where coverage is unreliable.

WHO IT'S FOR

Student pilots, PPL exam candidates, CPL and ATPL candidates, flight school students, and anyone preparing for KCAA or ICAO-aligned aviation theory exams.

Questions, corrections, or a subject you'd like added? Report a question from inside the app — we read everything.
```

## Category and tags

- **Category:** Education
- **Tags:** Education → Test Prep (add "Books & Reference" as secondary if offered)

## Keywords to keep present in the listing text

Play indexes the title, short description, and full description. Target phrases
(2026-09-21), each present at least once in the copy above: aviation exam,
aviation test, aviation test preparation, pilot training, pilot exam, PPL exam,
PPL, CPL, ATPL, student pilot, test prep, offline, KCAA, ICAO, air law,
meteorology, navigation, principles of flight.

What the copy cannot do: Play has no keyword field, and rank for a broad term
like "pilot training" is driven mostly by installs, rating and retention. The
levers that move that are the in-app review prompt (already on after three
passed exams), replying to every review, and screenshots that show explanations.
Keyword repetition in the title or short description is a policy violation and
gets the listing rejected — one occurrence each is the ceiling.

## Screenshots (highest-impact asset — 8 slots)

Play conversion is driven by the first two screenshots more than by any text.
Put a one-line caption on each image; don't ship bare screen captures.

1. Exam question in progress — caption: "7,900+ real exam-format questions"
2. Results screen with a pass — caption: "Instant scoring and explanations"
3. Subject grid — caption: "All 7 KCAA subjects covered"
4. Question with explanation — caption: "Every answer explained"
5. Report-a-question sheet — caption: "Flag a problem question in one tap"
6. Offline state — caption: "Works with no internet"
7. Progress/stats — caption: "See your weak subjects"
8. Free callout — caption: "Free — no exam limits"

## Feature graphic (1024x500)

Aircraft cockpit or horizon background, app name, and the single line
"7,900+ KCAA Exam Questions — Free & Offline".

## Release notes — v1.2.1 (versionCode 10)

Version code 9 (1.2.0) is already consumed on Play by the stale bundle uploaded
on 24 Aug 2026 — Play rejects a second upload with the same code — so the
shippable build is **10 (1.2.1)**. Same code, same question bank; only the
version changed.

**Release name (Play Console, internal only):** `10 (1.2.1) — explanations for every question`

Play's "What's new" allows 500 characters. Final copy, 427 chars, verified
2026-09-21. The logbook is hidden (`FeatureFlags.logbookEnabled = false`), so it
is not mentioned.

```
Every question now has a written explanation — 7,900+ of them — so you learn why an answer is right, not just which one.

• 775 answer keys corrected after a full review of the question bank
• Unlimited free practice in every subject, no exam limits
• Share your results with study partners and instructors
• Report a problem question straight from the exam screen
• Updated for Android 16, plus performance and stability fixes
```

If unlimited practice or question reporting was already in the live 1.1.1
build, delete that line; the explanations line is new in this release
regardless, because the audit that produced them finished on 2026-09-09.

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
