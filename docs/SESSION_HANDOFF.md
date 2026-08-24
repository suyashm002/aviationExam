# Handoff — Aviation Exam Pro

State as of 2026-08-24. Read this first in a new session.

## Ready to upload

`app/build/outputs/bundle/release/app-release.aab` — signed, verified.
versionCode 9 / 1.2.0, targetSdk 36, Play Billing 9.1.0.

Clears both 31 Aug 2026 Play deadlines (billing >= 8.0.0, targetSdk 36).
Rebuild after any change: `./gradlew :app:bundleRelease`

Release notes and store listing copy: `docs/STORE_LISTING.md`.

## Environment

- Build from the CLI only. Android Studio is Ladybug 2024.2 and cannot open
  AGP 8.13.2 — that is an IDE sync limit, not a build failure. Studio needs
  2025.2 (Otter) or newer; the download page misdetects macOS 26.5 arm64, so
  use "Download options" -> Mac (Apple silicon), or `brew install --cask android-studio`.
- Test device: Samsung SM-A146B (Android 15), connected via adb.
  `adb install -r app/build/outputs/apk/release/app-release.apk`
- `adb shell pm clear com.suyash.mockcivilaviationexam` wipes the login. Only
  use it when a question-bank reload is needed (see the flag issue below).

## NOT yet verified — do this before publishing

The Room v5 -> v6 migration and the logbook userId re-homing both run on
upgrade and both touch existing user data, with
`fallbackToDestructiveMigration(dropAllTables = true)` still in the builder —
a faulty migration wipes logbooks silently rather than crashing.

Only fresh installs have been tested. Either test a real upgrade over the live
Play build, or use a staged rollout of 10-20% and watch the crash dashboard.

## Question bank

7,948 questions in `app/src/main/assets/all_questions.tsv`, 10 tab-separated
columns. (Earlier notes claiming 9,076 were wrong — verified by row count and
device logs.)

Column 10 is `explanation`, rendered by `ResultsScreen.kt` when non-blank.
12 written so far, all Air Law. Workflow:

1. Author entries in `tools/explanations_cache.json` ({question_id: text})
2. `python3 tools/apply_explanations.py`

Do NOT use an LLM API for this — Claude writes them directly in-session. An
API-based generator was written and deleted as unnecessary.

### Answer-key quality

Writing explanations doubles as an audit of the answer key. Of the first 12
Air Law questions, 1 marked answer was wrong (~8%). If that rate holds there
are several hundred bad answers in the bank.

Corrections are auditable in `tools/answer_corrections.json` with a reason and
a `from` value that is verified before writing, applied by
`python3 tools/apply_corrections.py`.

Applied so far:
- `0Gf8pmb4PTH6AL7OJcMg` B -> C (stem is the ICAO definition of airside;
  "Terminal" was wrong)

Reviewed and deliberately NOT changed:
- `0lfw6se01Qq66VP5fwqq` stays B (MET). The option list includes AGA, which
  only exists in the legacy AIP structure where MET was a real part. Correct
  as framed. Check the frame of reference before "fixing" an answer.

## BLOCKER — explanations cannot reach existing users

`QuestionCacheManager.kt:71` guards the TSV import with the SharedPreferences
flag `tsv_data_loaded_v1`. Upgrading users already have it set, so the TSV is
never re-read and no explanation or answer correction ever reaches them —
only fresh installs.

Fix before shipping any question-bank change: version-stamp the flag (e.g.
derive it from an asset version constant) so a new asset forces one reload.
Note the reload re-parses 7,948 rows on first launch after upgrade, so keep it
off the UI thread and measure it.

## Known defects (not blocking 1.2.0)

- Logbook is built but hidden behind `FeatureFlags.logbookEnabled = false`.
  Two defects block re-enabling:
  - `LogbookViewModel.loadFlights()` sets `isLoading = true`, but only the Room
    Flow collector clears it. Returning from the entry screen without saving
    produces no emission, so the spinner never stops.
  - `ProfileViewModel` is a stub — profile edits do not persist, so the
    exported PDF header carries no licence number.
- Paywall is off (`FeatureFlags.subscriptionEnabled = false`) — deliberate,
  growth-first. Billing code stays compiled in and Play-compliant.

## Deferred by the user

- Referral system — next release
- Multi-authority expansion (DGCA/EASA/etc.) — next release. Biggest lever on
  downloads, but confirm the provenance of the question bank first: a DMCA
  takedown over copied questions is the realistic suspension risk, not the
  branding.

## Nothing is committed

The repo has a single commit at versionCode 1. Everything above lives in the
working tree only. Consider committing before further work.
