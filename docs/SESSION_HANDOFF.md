# Handoff — Aviation Exam Pro

State as of 2026-08-26. Read this first in a new session.

---

## START HERE — next action

The question-bank audit is 25% done (3 of 7 sections). Next section is
**operational_procedures** (885 questions).

All shell/python snippets in this file assume the repo root as the working
directory. Each has been run and verified as written.

```bash
# 1. Dump the section to a working file
python3 - <<'EOF'
import csv
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
h = rows[0]; i = {k: n for n, k in enumerate(h)}
n = 0
with open('/tmp/ops.txt', 'w', encoding='utf-8') as f:
    for r in rows[1:]:
        if r[i['sectionId']] != 'operational_procedures': continue
        n += 1
        f.write(f"[{n}] {r[0]}\nQ: {r[1]}\nA) {r[2]}\nB) {r[3]}\nC) {r[4]}\nD) {r[5]}\nKEY: {r[6]}\n\n")
print(n, 'written')
EOF

# 2. Work through it in batches of ~30, authoring into the two JSON caches
# 3. Apply, then bump TSV_ASSET_VERSION
python3 tools/apply_corrections.py
python3 tools/apply_explanations.py
```

Read **"How to audit a section"** below before starting — the cross-check
technique there found about a third of all corrections.

---

## Progress

| Section | Questions | Explanations | Corrections | Error rate |
|---|---|---|---|---|
| air_law | 817 | 817 | 114 | 14% |
| principles_of_flight | 671 | 671 | 97 | 14.5% |
| meteorology | 509 | 509 | 65 | 13% |
| **operational_procedures** | **885** | — | — | **next** |
| navigation | 1,090 | — | — | |
| human_performance | 1,417 | — | — | |
| aircraft_general | 2,559 | — | — | |
| **Total** | **7,948** | **1,994 (25%)** | **276** | |

5,951 questions remain. The wrong-answer rate has been measured at 13–14.5%
across three independent sections, so expect roughly **800 more bad answers**.

Suggested order: smallest first — operational_procedures, navigation,
human_performance, aircraft_general.

---

## How to audit a section

**Workflow**

1. Author into `tools/explanations_cache.json` (`{id: text}`) and
   `tools/answer_corrections.json` (`{id: {from, to, reason}}`)
2. `python3 tools/apply_corrections.py` — safe, verifies `from` before writing,
   so it cannot double-apply. Previously-applied corrections show as "skipped",
   which is expected and healthy.
3. `python3 tools/apply_explanations.py`
4. Bump `TSV_ASSET_VERSION` in `QuestionCacheManager.kt`

**The single most productive technique: cross-check near-duplicate questions.**
This bank contains many mirror pairs asking the same thing with the same options.
Where two disagree, one is provably wrong, and the pair tells you which. About a
third of the meteorology and principles-of-flight corrections were confirmed this
way, and each such correction cites the sibling question's id in its `reason`.

**Second technique: check the stem for a constraint that eliminates options.**
A jet-stream question keyed to "headwind" was refuted by its own stem saying
"crossing at right angles" — geometrically it can only be a crosswind.

**When NOT to correct.** Establish the frame of reference first. One Air Law
question looked wrong against the current AIP structure but its option list
included AGA, which only exists in the legacy structure where MET was a real
part — the marked answer was correct as framed. Where the question is a soft
quantitative judgement, or two options are equally defensible, leave the key
alone and say so. Several such cases are listed under "Open questions" below.

**Verify after applying:**

```bash
python3 - <<'EOF'
import csv, json
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
co = json.load(open('tools/answer_corrections.json'))
ai = rows[0].index('correctAnswer')
bad = [r[0] for r in rows[1:] if r[0] in co and r[ai] != co[r[0]]['to']]
print("corrections not reflected:", len(bad))
print("all rows 10 cols:", all(len(r) == 10 for r in rows[1:]))
EOF
```

---

## What has been audited

### Air Law — 100% complete

817/817 explanations (814 real + 3 intentionally blank), 114 corrections (14%).

Error categories: airspace classification (Class D vs E, F vs E), interception
signals, AIP section placement, transponder codes (7500/7600/7700), VMC minima,
distances and times, and definition confusions (ASDA/TODA/TORA, CTA vs CTR,
base turn vs procedure turn).

### Meteorology — 100% complete (2026-08-26)

509/509 explanations, 65 corrections (13%).

- **Sign errors in altimetry** — pressure correction the wrong way round
  (`0jPIfeFcJkuYFXwBLVOU`, `sSp0O10sABZP62NqNDee`)
- **Constant-pressure chart / flight level mismatches** — 200 hPa keyed to FL300,
  850 hPa keyed to FL290 (`eXUMw1eobBpUDS4JmcwW`, `ZJsvIJYB7LeEOTSPgGIb`)
- **Surface-wind veer/back rule** — direction unchanged, or backed instead of
  veered (`L1hEu64xVgNYLQmOXOw9`, `QAFJCsgrwVe4OCl7qmP3`)
- **TAF/METAR decoding** — TEMPO and PROB30 groups ignored, gust read as the top
  of a mean range, BECMG timing misapplied (`tdNKKbIwaqGTD5ccSxHU`,
  `zeiLXrN6gyi4qhqv49an`, `Rqg5Mdyjdi9h132CBHrb`, `YogDYnMnBEAJwJV4Nyk3`)
- **Cloud composition and icing** — high cloud keyed as supercooled droplets,
  drizzle keyed to altostratus, rime/clear ice reversed
- **Definitions reversed** — katabatic as a daytime wind, sublimation as melting,
  NOSIG as "not signed by the meteorologist"

### Principles of flight — 100% complete (2026-08-26)

671/671 explanations, 97 corrections (14.5%) — the worst rate so far.

**Several of these taught an actively unsafe procedure, not merely a wrong fact.
Treat this work as a safety audit, not a copy-edit:**

- **Stall recovery keyed to "idle power and no other corrections"**
  (`1tlKvPaC5LI5ZHHStQMC`, `vcNggN08D5rb7atr2w4b`) — correct is full power,
  ailerons neutral, wings levelled with rudder.
- **Spin recovery keyed to "stick pulled to the most aft position"**
  (`hyZHPFiqnSvMuN5mKUBU`) — that sustains a spin.
- **"Engine failure at or above V1 → reject if below VR"** (`g8CVY9YoxoRpbHFh5Iwl`)
  — the exact overrun scenario V1 exists to prevent.
- **VMCA keyed as requiring altitude and 100 ft/min climb** (`rykoEDvCYDwDzo9cIPFu`)
  — CS-25.149 requires only straight flight.

Systematic categories worth watching for in the remaining sections:

- **Reversed sign/direction** — CP moving aft instead of forward with alpha;
  dihedral "decreasing" lateral stability; density halved "reducing drag by 1.4";
  shock-wave density "decreasing"; constant-Mach descent TAS blamed on pressure
- **Jet vs propeller speed regimes** — max range keyed at (L/D)max (the PROPELLER
  case); minimum-power speed keyed above minimum-drag speed
- **Certification definitions** — take-off run; clearway credited to ASDA instead
  of TODA; second segment keyed with gear DOWN and flaps retracted
- **Arithmetic** — gust load factor; holding fuel-flow scaling; 45°-bank stall
  speed increase keyed 31% (it is 19% — 41% is the LIFT increase)

---

## Bank-wide data defects (scanned 2026-08-26)

Scan of the whole TSV, so this covers the unaudited sections too.

### Image-dependent questions — 16 total, 13 in sections NOT yet audited

Unanswerable in a text-only app. **Decide the policy before starting navigation**,
since that section alone has 8:

| Section | Count | Status |
|---|---|---|
| navigation | 8 | not yet audited |
| aircraft_general | 3 | not yet audited |
| operational_procedures | 2 | not yet audited |
| principles_of_flight | 3 | audited — `j3vbgqBvVbKVmr7aHwfp`, `rnYhFaq0tNdQBTlXCHaw`, `zfulgMmT5Rur03N123kZ` |

The ids in the sections still to audit:

- **navigation (8):** `8iQwwWLJSrMANOeykfyH`, `bC1yhOAegB6kcz4t7o35`,
  `NcSKwheZNLCsDGITHTl0`, `OcGbsvFhze9mczc5YcFz`, `qKqKf8oje4qHHCZMw5Xy`,
  `rVsdNMQ2p5ukMRrJO6CU`, `u5DiiqacblfuRYpmhG48`, `z08mQj8E0h402yYbv4du`
  — mostly Air Almanac / twilight and position-fix plotting questions
- **aircraft_general (3):** `3BRrS0xVDdbE1LyU3o69`, `3cONw9JS8W4plsQ1DqUz`,
  `yn3S4vqpIsOaF2TOteIr` — hydraulic system schematics
- **operational_procedures (2):** `1JqXlrk6F9uuVMmsq9yN`, `lgkzG8zXAxhwObQHPSgU`

For the three in principles_of_flight, explanations were written to teach the
underlying concept instead of answering the missing diagram. Same approach will
work elsewhere, but dropping them from the TSV is cleaner if you would rather.
Note the navigation ones are the least salvageable — several require reading
values off an almanac extract, so no concept-only explanation really substitutes.

Regenerate the list with:

```bash
python3 -c "
import csv, re
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
for r in rows[1:]:
    if re.search(r'\bimage\s*\d+|refer to image', r[1], re.I): print(r[7], r[0], r[1][:70])
"
```

### Blank option D — 69 total

54 in air_law, 15 in principles_of_flight, **none in the unaudited sections**.
Keys are still correct; the missing option simply reduces these to three-choice
questions. The 15 in principles_of_flight are `0QW52e6aikKs75YMlzZ7`,
`5JPWZQzo3Imn5DumSd69`, `aswNozoQZobBagf2ZIEt`, `aMoNPwTEIAQfUhHSlpN9`,
`FAaFlFz1n6iWk35PFVGm`, `edC3pMBWHPOdVNoOg5wI`, `I1JP7okftWTqVbCP8vi6`,
`IJPzezETUeluzJG8PuTu`, `oXCsHYZmF1ohIhHcVXXl`, `LO0xR8OMQiFM7aqfu7HS`,
`PDTyq5GvaLBX1urMgzhZ`, `RIGcRcHBHx80Xo5LoVZp`, `RStQvodvqTKmspotjqi2`,
`wnhLuYN6ji8fqOJDxdRm`, `ZAQmC41HBQ6WSoUxi20D`.

### Corrupted question text

`eC7EcJlARNi2lkStFbWR` (meteorology) has its question text collapsed into
option B by a parsing defect. The key is still correct.

---

## Open questions — left as keyed, need a decision

These were NOT changed, because a correction would have been a guess rather than
an audit. Each is genuinely ambiguous as posed:

- **Two near-identical questions keyed to DIFFERENT options, both statements
  true:** `mmHD0wsMTVbEBoS08iJp` vs `Ung19E8mcWDOZTmkTxAW` (servo tab), and
  `kk2YmZMPKtvE3W7c4Dln` vs `ZQLcmwSXw1CZzn9y1aHs` (elevator position when
  trimmed).
- **Flap pitching moment answered both ways:** `p0QNEqMVkZWjuugYQ84W` (nose up)
  vs `jbhdZXNVlcoRAvqQOz2w` (nose down, Fowler). Both defensible — the net moment
  depends on flap type and tailplane position.
- `mK7ENZlFBoo26QgZ3SLW` asks for maximum glide TIME but offers only minimum-drag
  and minimum-glide-angle options, which are the same condition and both give
  maximum DISTANCE.
- **Duplicate options within one question:** `DRe8FO6lQv1gcJJrRC5i`,
  `dtDceDNaEyUSKKiepmFh` (two identical option texts), `sYTdsagu5TcuY8yF9pNy`
  (A and C identical), `AxewD2T5EYHgSu9TBXdx` (A and D equivalent),
  `GoSpYoy3q12S1LTFZ1N6` (C and D equivalent in level flight).
- `0lfw6se01Qq66VP5fwqq` (air_law) stays B (MET) — correct as framed against the
  legacy AIP structure. Check the frame of reference before "fixing" an answer.

---

## Build state — REBUILD NEEDED before uploading

The AAB from an earlier session is stale: the TSV has changed twice since
(meteorology, then principles of flight) and `QuestionCacheManager.kt` has
changed.

```
./gradlew :app:bundleRelease
```

versionCode 9 / 1.2.0, targetSdk 36, Play Billing 9.1.0.
Clears both 31 Aug 2026 Play deadlines (billing >= 8.0.0, targetSdk 36).

Release notes and store listing copy: `docs/STORE_LISTING.md`.

### TSV version stamp

`QuestionCacheManager.kt` compares an integer `tsv_data_version` against
`TSV_ASSET_VERSION`, currently **3**. Version 3 covers BOTH the meteorology and
principles-of-flight passes, because no build was produced between them.

**Bump `TSV_ASSET_VERSION` whenever the TSV changes** — existing users re-parse
the TSV on next launch when the stored version differs.

---

## NOT yet verified — do this before publishing

The Room v5 → v6 migration and the logbook userId re-homing both run on upgrade
and both touch existing user data, with
`fallbackToDestructiveMigration(dropAllTables = true)` still in the builder — a
faulty migration wipes logbooks silently rather than crashing.

Only fresh installs have been tested. Either test a real upgrade over the live
Play build, or use a staged rollout of 10–20% and watch the crash dashboard.

---

## Question bank facts

7,948 questions in `app/src/main/assets/all_questions.tsv`, 10 tab-separated
columns. (Earlier notes claiming 9,076 were wrong — verified by row count and
device logs.)

Column 10 is `explanation`, rendered by `ResultsScreen.kt` when non-blank.

**Do NOT use an LLM API for explanations — Claude writes them directly
in-session.**

All corrections are auditable in `tools/answer_corrections.json` with a written
reason and a `from` value that is verified before writing.

---

## Environment

- Build from the CLI only. Android Studio is Ladybug 2024.2 and cannot open
  AGP 8.13.2 — that is an IDE sync limit, not a build failure. Studio needs
  2025.2 (Otter) or newer; the download page misdetects macOS 26.5 arm64, so use
  "Download options" → Mac (Apple silicon), or `brew install --cask android-studio`.
- Test device: Samsung SM-A146B (Android 15), connected via adb.
  `adb install -r app/build/outputs/apk/release/app-release.apk`
- `adb shell pm clear com.suyash.mockcivilaviationexam` wipes the login. Only use
  it when a question-bank reload is needed.

---

## Known defects (not blocking 1.2.0)

- Logbook is built but hidden behind `FeatureFlags.logbookEnabled = false`.
  Two defects block re-enabling:
  - `LogbookViewModel.loadFlights()` sets `isLoading = true`, but only the Room
    Flow collector clears it. Returning from the entry screen without saving
    produces no emission, so the spinner never stops.
  - `ProfileViewModel` is a stub — profile edits do not persist, so the exported
    PDF header carries no licence number.
- Paywall is off (`FeatureFlags.subscriptionEnabled = false`) — deliberate,
  growth-first. Billing code stays compiled in and Play-compliant.

## Deferred by the user

- Referral system — next release
- Multi-authority expansion (DGCA/EASA/etc.) — next release. Biggest lever on
  downloads, but confirm the provenance of the question bank first: a DMCA
  takedown over copied questions is the realistic suspension risk, not the
  branding.

## Source of truth: the JSON caches, not the TSV

`app/src/main/assets/all_questions.tsv` is **gitignored** (`.gitignore:30`), so
the question bank is not under version control. The audit work is stored in the
two tracked caches instead:

- `tools/explanations_cache.json` — 1,994 explanations
- `tools/answer_corrections.json` — 276 corrections, each with a `from`, `to`
  and a written reason

The TSV is a build artifact regenerated from those by `apply_corrections.py` and
`apply_explanations.py`. **If the TSV is ever lost or reverted, re-run both
scripts to rebuild it** — no work is lost. Conversely, editing the TSV directly
would lose the change on the next apply, so always author into the caches.

A fresh clone will not have the TSV at all. Given the DMCA/provenance concern
noted under "Deferred", keeping the copied question text out of the repo is
probably deliberate — worth confirming before changing it.

## Commit state

Committed on `release/1.2.0-play-compliance` (2026-08-26): the two caches, the
`QuestionCacheManager.kt` version-stamp change, and this handoff.

Still uncommitted and unrelated to the audit: `gradle/wrapper/gradle-wrapper.properties`
(Gradle 8.14.3 → 8.14.5, bumped by the wrapper itself). Left alone deliberately —
commit it separately if wanted.
