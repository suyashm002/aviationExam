# Handoff — Aviation Exam Pro

State as of 2026-09-21. Read this first in a new session.

---

## START HERE — next action

**The question-bank audit is finished.** All 7,948 questions in all seven
sections have a written explanation, and 775 wrong answer keys have been
corrected. There is no section left to audit.

What remains before this can be shipped (2026-09-21 status):

1. **DONE** — release bundle rebuilt from the final TSV as **10 (1.2.1)**
   (`app/build/outputs/bundle/release/app-release.aab`, 21 Sep 2026, 9.6 MB).
   Version code 9 was already used on Play by the stale August upload.
   Unit tests pass. The signed release APK was installed on the Pixel_4_API_Tiramisu
   emulator and logcat showed `TSV asset version changed (stored=0, current=7) —
   reloading` followed by `Successfully loaded 7948 questions across 7 sections`.
2. **DONE** — `docs/STORE_LISTING.md` no longer advertises the logbook, which is
   hidden by `FeatureFlags.logbookEnabled = false`. The logbook stays hidden for
   this release by decision (2026-09-21).
3. **DONE** — upgrade path v4 → v6 verified on an emulator with real logbook
   data; see "Upgrade path — VERIFIED" near the bottom.
4. **DONE** — branch committed and pushed to origin (2026-09-21).
5. Spot-check explanations in the exam UI after signing in (needs a Firebase
   account; not done from the emulator).
6. Work through "Open questions" below — those keys were deliberately left
   alone and want a subject-matter decision, not another audit pass.

Build with the right JDK or Gradle fails instantly with the bare message `25.0.2`:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-23.jdk/Contents/Home
./gradlew :app:bundleRelease
```

If you are picking up a *new* audit task (a new question source, say), read
**"How to audit a section"** below — the sibling cross-check technique
described there found 63 of the 133 navigation corrections, roughly half of the
89 in human performance, and about 110 of the 162 in aircraft_general.

---

## Progress

| Section | Questions | Explanations | Corrections | Error rate |
|---|---|---|---|---|
| air_law | 817 | 817 | 114 | 14.0% |
| principles_of_flight | 671 | 671 | 97 | 14.5% |
| meteorology | 509 | 509 | 65 | 12.8% |
| operational_procedures | 885 | 885 | 115 | 13.0% |
| navigation | 1,090 | 1,090 | 133 | 12.2% |
| human_performance | 1,417 | 1,417 | 89 | 6.3% |
| aircraft_general | 2,559 | 2,559 | 162 | 6.3% |
| **Total** | **7,948** | **7,948 (100%)** | **775** | **9.8%** |

The wrong-answer rate ran 12–14.5% in the four smaller technical sections but
only 6.3% in both of the two largest, human_performance and aircraft_general.
The pattern is about redundancy rather than subject: the big sections carry many
near-duplicate questions, and a key that disagrees with its siblings is easy to
spot and was usually the only thing wrong. Where a section had few duplicates,
errors survived unnoticed.

Verification after the final apply (re-run any time):

```
7948 rows, all 10 columns
7945 explanations in the TSV, 3 deliberately blank
775 corrections, 0 unreflected
```

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

**The batch workflow.** `tools/merge_batch.py` (written 2026-08-31) now exists
and should be used. It takes one JSON file of the form

```json
{"explanations": {"<id>": "text", ...},
 "corrections":  {"<id>": {"from": "A", "to": "B", "reason": "..."}, ...}}
```

and validates before it writes anything: every id must exist in the TSV, each
correction's `from` must match the key currently in the TSV, `to` must differ and
be A–D, and a written reason must be present. It refuses the WHOLE batch on any
failure, which stops a mis-typed id silently doing nothing. Batches of ~43
questions worked well: one read of the dump, one authored JSON, one merge.

**Do NOT run two merges concurrently.** The helper does read-modify-write on the
two caches, so two merge processes issued in the same turn will silently clobber
each other — four batches were lost this way and had to be re-merged. Verify at
the end of a section with:

```bash
python3 - <<'EOF'
import json, glob, csv
ex = json.load(open('tools/explanations_cache.json'))
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
i = {k: n for n, k in enumerate(rows[0])}
sec = [r for r in rows[1:] if r[i['sectionId']] == 'human_performance']
print(sum(1 for r in sec if r[0] in ex), '/', len(sec))
EOF
```

**The single most productive technique: cross-check near-duplicate questions.**
This bank is full of mirror pairs — the same question with the options shuffled.
Where two disagree, one is provably wrong, and the pair usually tells you which.
Where three or four siblings agree against one, the odd one out is the error.
Most of the 115 operational-procedures corrections cite a sibling id in the
`reason` field.

Start with a programmatic scan, but do not stop there:

```bash
python3 - <<'EOF'
import csv, re
from collections import defaultdict
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
i = {k: n for n, k in enumerate(rows[0])}
sec = [r for r in rows[1:] if r[i['sectionId']] == 'navigation']
norm = lambda s: re.sub(r'[^a-z0-9]+', ' ', s.lower()).strip()
g = defaultdict(list)
for r in sec: g[norm(r[1])].append(r)
for k, v in g.items():
    if len(v) < 2: continue
    txt = {norm(r[{'A':2,'B':3,'C':4,'D':5}[r[6].strip().upper()]]) for r in v}
    if len(txt) > 1:
        print(v[0][1][:90])
        for r in v: print('  ', r[0], r[6], r[{'A':2,'B':3,'C':4,'D':5}[r[6].strip().upper()]][:80])
EOF
```

The scan normalises whitespace, so it MISSES pairs whose stems differ by a
stray space inside a word — the source PDF is full of them ("wak e turbulence").
Two real conflicts in operational_procedures were found only by reading, not by
the scan. Read every question anyway.

**Second technique: check the stem for a constraint that eliminates options.**
A question asking which agent suits a *class B* fire cannot be answered "all
four" when water is one of them.

**Third: watch the units and the direction of an effect.** 243 MHz offered as a
"VHF" frequency, an option claiming ice *increases* roll rate, a shear question
pairing "above the path" with "decreasing airspeed" — each is refuted by its own
internal contradiction without any reference material.

**When NOT to correct.** Establish the frame of reference first. Two questions
here key noise-abatement procedure B for close-in noise relief, which is the
reverse of the modern PANS-OPS NADP 1 / NADP 2 mapping — but it matches the
older Doc 8168 text the bank is written from, and the bank is self-consistent
across both questions, so the keys were left alone. Where the question is a soft
quantitative judgement, or two options are equally defensible, leave the key and
say so. The cases left alone are listed under "Open questions" below.

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

### Navigation — 100% complete (2026-08-31)

1,090/1,090 explanations, 133 corrections (12%). 63 of those corrections cite a
near-duplicate sibling — this section is riddled with mirror pairs, and the
cross-check found roughly half the errors on its own.

**Systematic categories, in rough order of yield:**

- **Sign and direction reversals in the heading/track chain.** Drift applied the
  wrong way (`yDhL2pcZ4fnzDsNQDfL9`, `ySOTuKklw10vzdaX9FiQ`, `zkLwrgcZatj8fvvYuNZ3`,
  `UsjqnCFq6YkzYAc28UzO`), variation reversed (`Fem8la2rabKSlA4dxggq` had True
  north west of Magnetic), and the ANDS acceleration rule inverted
  (`fkBbv0tJgV9jd2n6C3sq`, `jDx87rMt7FVTG7jsJswE`). Turning error was keyed as
  over-indicating on a turn through NORTH (`qMKBuhGPy7QSRherHmSS`).
- **Line-of-sight range formula.** Five questions dropped the 1.23 factor or
  invented a value: `b2fTPTJquVoPqgy7iXhm`, `rnyab0I1JS0oQjcAUvxx`,
  `SScvfhMQC5KuzOnXqehH`, `vdm1GTi4vTQIjSrguM9w`, `vKRy0ORNNkbcjW8TlVd3`, plus
  `Voi4kcUhnYl7TE6WNKjX` (minimum level for a given range) and
  `vB5j1wHJYzyOluKT43vJ` (123 NM keyed as 1230 km).
- **Units.** DME quoted in kHz instead of MHz (`mKrZdIz0jssT5knVKO3d`); a
  parallel's circumference keyed longer than the equator (`moqE4YhD9vOuxZqpYcGr`);
  chart distances left in kilometres and labelled NM (`karmx4Yb1boUKNEdpyUE`,
  `3favCMEKlmtWtTqf5Q2v`); air miles keyed GREATER than ground miles with a
  tailwind (`imt5fAtlN3wg6UA48Oey`).
- **1-in-60 problems that stop at the track error** and never add the closing
  angle (`IsNuqfKgpnN8g4wRPk2c`).
- **Fuel policy.** Final reserve keyed at 45 min or above MSL rather than 30 min
  at 1500 ft above AERODROME elevation (`5EhsEvK4QAgS6cceV1Ak`,
  `il0jXMkAkh2v75M8y8zg`, `pLlxHtAdHBp6ZuwS2Rtm`); contingency omitted from a
  ramp-fuel build-up (`lbYEYfVdNTV5C9yPyjxY`, `PwE1kFycEwW8Cxeg8IZX`); reserve
  fuel spent to reach destination (`YXeQiohLCcisPx1S8DiS`); alternate landing
  mass with alternate fuel ADDED instead of subtracted (`4AVvTLLMeoJe0wjFhYTi`).
- **Radio theory.** SSR radio failure keyed 7700 (`cZfB0JUvtvD92dr4ixe1`); PRN
  described as atmospheric jamming (`mSZcVKTqTZdAj0iXTG4j`); GPS orbital
  inclination measured from the earth's axis (`hAdR68SxQICfZ2aY3zgc`); MLS azimuth
  coverage keyed +/-20 and +/-30 instead of +/-40 (`Oj4va9Lj52EPNg1IKiU3`,
  `vbcNYscT4M0BnJGTwCY8`); night HF frequency keyed high instead of low
  (`sstHByo6TQREXwayP4Qw`).
- **Definitions swapped between instruments.** Gyro "drift" keyed as wander in
  ANY plane rather than the horizontal (`xD3cjfddASHZDab61Gow`); quadrantal error
  attributed to VOR (`ZoXn8eyVo8S47UjTZGge`); a RLG keyed as free of lock-in
  (`6ugebKVo8bfObBBnnwvL`) while its own sibling explains dither.
- **Chart geometry.** Great circles on a Lambert keyed concave to the POLE
  (`9577z1bvzcHmgjx70Imi`); Lambert parallels keyed as straight lines
  (`zENscEKrpT97VxvukRj4`); transverse Mercator parallels keyed straight when the
  stem excludes the equator (`1QZPMpyda5OWbTRj9dah`); the great circle between two
  points on a parallel keyed EQUATORWARD of it (`WkXhLsEhSoWdOj1AC5X8`).
- **A corrupted row that keyed a stem fragment as the answer**
  (`bkRS48ec8ZdBuqRQFQrE`).

### Human performance — 100% complete (2026-09-08)

1,417 explanations, **89 corrections (6.3%)**. Much the lowest error rate of any
section so far — see the note in Progress above for why.

Systematic error categories found:

- **Reversed or inverted definitions.** The commonest single class. Hyperventilation
  keyed as a *surplus* of CO2 (`gIah2cJXD585445SJ5ap`) and as producing no acid-base
  change at all (`VrNcvzYSPSSsf5hiXeSq`); confirmation bias keyed as favouring
  *contradictory* information (`yrKq3qnRtcmCgYtgJYds`); acute stress keyed as a
  *decrease* in mobilised resources (`BqZ1e7OjxiDVR241BZap`); linear acceleration
  keyed as feeling like a descent rather than a climb (`reu3mk2BDD0FWulzpFjK`);
  arousal/performance keyed as "approximately linear increasing" instead of the
  inverted U (`bo2jmNHRoW4lYutgE5Wz`).
- **Vestibular / illusion mix-ups.** Spin recovery keyed as a sensation in the
  original direction (`lZiu1Yp6IwjimG7DA4Wh`); the Coriolis illusion keyed as
  "pressure vertigo" (`fpgCkUbZsh48cPqLm3Qq`); a fading light keyed as a climb
  illusion (`xmquChWJwnDZiRNTK7RM`); acceleration in level flight keyed as a
  sensation of rotation (`vANZdEzZWbe6g6r65iSO`); the vestibular system keyed as
  containing "two ventricles" (`f3Vc9ORPBrW7fWNhrcJx`); radial acceleration keyed
  along the transverse axis instead of the vertical (`VHXUvsc8SA8VXyDeiAnM`).
- **Wrong anatomy.** The Eustachian tube keyed to the paranasal sinuses
  (`oTfp7qEYNTiswU7bOggu`); accommodation attributed to the retina
  (`pBR0xx59rmdtufaA3tXU`); blood pressure keyed as measured by the arterioles
  (`mmhwcIE49v4cpIZoAVO5`); respiratory drive keyed to alveolar water vapour
  (`yUoiNDjQu4cklbnyOlyC`); the inner ear keyed as sensing linear acceleration only,
  excluding hearing (`w31IYmjr6qciHY1qM4E6`); creeps and bends transposed
  (`x5Xeq0fKjM7sJJUrKg7X`); conductive hearing loss keyed to include auditory-nerve
  damage (`X4tEAlxPbd7jr116tEdB`); gut barotrauma keyed as "barotitis"
  (`rviYaoeALsnrX0i3gn65`).
- **Numbers.** 100% oxygen keyed as sufficient to 45,000 ft rather than ~38-40,000
  (`6AhVhaliyz79MSPTsU0w`); the hypoxia critical threshold keyed at 38,000 ft in a
  non-pressurised aircraft (`MkzrKBlleC5NUM2eKCIk`); alcohol elimination keyed at
  0.02-0.05% per hour instead of 0.015% (`x7T87kvadpNyzTEp8TET`); the simple-task
  human error rate keyed at 1 in 50 instead of 1 in 100 (`8cxdDE2GEPhSKLpPDWUF`);
  argon and CO2 transposed in the composition of air (`gx9IDHGQgZ9xK0vR44vn`);
  post-dive delay keyed at 48 h for a no-stop dive (`K7W4JF50H4pcw5Y52mju`).
- **Multi-statement sets with one wrong member.** Ten or so corrections were of the
  form "the marked combination includes a statement that is false, or omits one
  that is plainly true" — e.g. `BQROPcJgkX00dWHGzt7X` (exercise "reduces tolerance
  to hypoxia"), `Z4bkJyQsgMK5JpkoY2CX` (blood pressure not depending on the work of
  the heart), `F427w1mfM5wCA2URhWrC` (aggression omitted from the behavioural
  effects of stress), `nGsgyRM6vW1Lux19yf8f` (short-term memory "unlimited in time
  and insensitive to disturbance"), `YX3DnVkE2O13hbHqASh6` (sustained monitoring
  assigned to people rather than machines).
- **CRM answers that contradict CRM.** High automation "guarantees" situational
  awareness (`TSjtVTNummcFbADcluDP`); the copilot who feels unfairly treated should
  "internally retire and think positive" (`gzCrhEDILM0L2FvYfmsS`); SOPs should not
  reflect an operator's cockpit philosophy (`lxVa6mFR6wjuciktxW4X`); over-the-counter
  medicines have "no side effects which would give problems to a pilot"
  (`cwFMM25AEviKvsiivxan`); overload met by abandoning automatic processing
  (`uzytCCwnyMKkqBOFlIdW`).

Cross-checking siblings did most of the work here: 60 of the 89 corrections cite a
near-duplicate whose key settles the matter. Where the bank keyed the *same* answer
twice, the key was left even when it looked wrong — see Open questions.

### Operational procedures — 100% complete (2026-08-30)

885/885 explanations, 115 corrections (13%).

**Safety-relevant corrections — the marked answer taught an unsafe action:**

- **Windshear handling, four separate questions.** `0nIk3kAqicOkeI7hQspt` keyed
  "reduce thrust rapidly" on an approach with reported shear; `CYu4vyFMZ4vbxdUu0D7p`
  keyed the required control input as "small"; `vJSDGgGNyG6CPT7oXJVM` keyed
  retracting gear and flaps and flying for best L/D during the escape;
  `7dCMoSI9SF0FNMVJAHMs` reversed the sign of a decreasing-headwind shear
  ("flies above the climb-out path").
- **`Ct4B2Srqy4Gh6K493KcM` — EPR with an iced inlet probe keyed as reading LOW.**
  It reads HIGH, and the crew then sets less thrust than it believes. This is
  the Air Florida 90 mechanism, keyed backwards.
- **`1igasgP8jZCP0LPaMPDp` — bomb on board keyed to CLIMB** to the highest level
  not needing pressurisation, which maximises the differential the fuselage is
  carrying. The manoeuvre is a descent to the indicated cabin altitude.
- **`IPefYQvkrnwnBtDZNzaW` — smoke in the air conditioning keyed to "determine
  which system is causing it"** before donning masks and goggles.
- **`gqcbyuJzXbBBp2FZNQeH` and `VhIWxzAMfTb1V0NREeaX` — hot wheels keyed to be
  approached from the SIDE**, the one direction a bursting tyre vents into.
- **`2YzlQGIGxxnnOxOPscJg` and `CUHwJgC3Ov8lMiSMk63x` — departing behind a landing
  heavy keyed to rotate BEYOND its touchdown point**, i.e. inside the vortex-laden
  approach path; and `n4xpcC8M1aOjJ1y9PjpV` keyed the departure path downwind of
  and below a wide-body.
- **`0wTXCxHq3mFzt0bxk6ww` — adult brace position keyed as "cross the arms in
  front of the face"**, which restrains nothing.
- **`wYGRBbCq6SHZNpITtZU1` — airframe ice keyed as increasing ROLL RATE**
  (it increases stalling speed and degrades roll response).
- **`rvzweKvnVMVZ8EqF8XNp` — passenger oxygen in cabin smoke keyed to "the oxygen
  would explode"**; the real reason it is useless is that the continuous-flow
  masks mix cabin air, smoke included, with the oxygen.
- **`8c3U6jzrIEmIDScDhymt` — 14 000 ft keyed as the highest altitude at which
  efficiency is unimpaired without oxygen**, and `hVvr2lCScgp8uK6w9pEA` keyed the
  time of useful consciousness at FL400 as a full minute (it is 12–18 seconds).

Systematic categories, useful as a checklist for the remaining sections:

- **Wake turbulence minima confused between cases** — the 2-minute (MEDIUM behind
  HEAVY) and 3-minute (LIGHT behind MEDIUM, and any intermediate-point departure)
  minima were swapped in five questions, and 5 NM / 6 NM likewise.
- **Extinguisher agent tables** — "all four" answers that silently include
  burning metals for CO2 or dry powder; water included for a class B fire; CO2
  wrongly excluded from class A.
- **Recency and interval numbers** — 30 days for 90, six take-offs for three,
  90 days for 6 months, 90 minutes for 60, 30 minutes for two hours.
- **Approach and circling minima tables** — Category C values keyed against a
  Category B aeroplane, 230 m offered for a CAT IIIA RVR, take-off minima of
  150 m quoted without the multiple-RVR requirement that earns them.
- **Document ownership** — MEL vs MMEL reversed (operator vs manufacturer), MEL
  attributed to the State of registry, MEL applied after taxi has begun.
- **Grid navigation sign errors** — a free gyro's earth-rate error keyed +32.5°
  instead of −32.5°, and grid/true conversions keyed with the convergence
  applied the wrong way (`P0EQVqZHkJW1s2EOeNX0`, `uL5WMMxsNI3kyZq84EyE`,
  `wPD4EqmxuaOc2LpIwBmX`).

---

### Aircraft general — 100% complete (2026-09-09)

2,559 explanations, **162 corrections (6.3%)** — the last section, and the
largest. About 110 of the 162 were settled by a sibling: the bank carries whole
families of the same question with the options shuffled, and where one member
disagrees with two or three others the outnumbered key is the error. Several
families disagree *internally* and are listed under Open questions instead.

Systematic error categories found:

- **Arithmetic and unit slips in mass & balance.** `WOzbtbL4BjEOz75HD5Ou` keyed
  the CG at 20.18 in (the distance from the LE of MAC) instead of 645.78 in from
  the datum; `u6Uc0e9A3XwagAfQqti4` keyed a moment as 49 000 ÷ 7 rather than
  × 7; `XwHsGua8LHHQT6CPc6YY` keyed a take-off mass computed with *trip* fuel;
  `Y5NVnhLn71mtPcEFUnhM` keyed a take-off mass 2 000 kg above the MTOM stated in
  its own stem; `cGLCnhKqjn5hvmkYpULa` keyed the traffic load where the useful
  load was asked for; `noCHCVAE9TUjwo1Tl5B1` keyed 250 ft/min for a 3° descent
  at 100 kt (it is 500).
- **Reversed physical roles.** Oil and nitrogen swapped in the oleo strut
  (`WfpvRTbxMmyycVkID64g`, and `Z3Y5CoeH3ySv9mpp5yua` keying "springs" as the
  damper); the bootstrap order given as expand-then-cool (`esbH2nJDKlqo0NnvqPXs`);
  the impulse turbine's pressure drop put across the rotor instead of the nozzles
  (`zdP53fxY02M7PRJCakoP`); spoilers deploying on the *outside* of the turn
  (`vV3YQKLqsc7E0bAW3TAZ`); the alternator rotor carrying AC rather than DC
  (`hkDdTF9UtEFG2Xlw9OaN`); the pack cooling fan running in the cruise rather
  than on the ground (`Z2IrldnlskCflJSTT5Qm`, outnumbered three to one).
- **Alerting-hierarchy confusions.** Caution and warning definitions swapped in
  `iNm6GtW8l6Pr4wSFXVdc` and `wX7PWTrEsH6mpSL7wQ2q` — the second had the caution
  keyed to the *advisory* wording, which the bank's own `GevaIlZruo4eOl3qAWx1`
  assigns to advisories.
- **TCAS answers that contradict TCAS.** Horizontal resolution advisories keyed
  in `qzbjJLEhAMuiD03USPHw` and `t6vtVvK8S7WB0XujabKT` (TCAS resolves vertically
  only); the RA symbol keyed as a red *circle* (`vO3gJ8bK7NO66U8gcvjj`); TCAS II
  keyed as giving only a proximity warning (`cMV7NFtSz8E8Y9BAbft6`).
- **Compass and turn-indicator sign errors.** `oAW7ogOgAY2xxScmdYlc` and
  `uNaJyv19em4D9elWUUD6` both had the aeroplane turning the wrong way for the
  needle shown; `j1IbLxGg5YtgV0y4AXqp` gave a 180° turning error on an easterly
  heading, where the error is nil; `mf50peNnStas8JGUHmKc` made transport wander
  greatest on a *meridional* track, where it is zero; `wn6z7goS2JN0Lttp4hE9`
  denied any acceleration error on an east-west heading, where it is greatest.
- **Regulatory numbers that appear nowhere in the rule.** Oxygen masks required
  to exceed the number of *passengers* rather than seats + 10%
  (`lavzKr4l1FpTavZ85Wzm`, three siblings agree); crew oxygen above 14 000 ft
  instead of 10 000 (`heT3PtQL5UyY9M5Dym24`, three siblings); one pilot on oxygen
  above FL490 instead of FL410 (`sjpwa3uDvUZqDOTj59yo`); cabin altitude limited to
  6 000 ft instead of 8 000 (`yrjdwiZDtUlL2Z8aMDFn`); FDR duration 30 minutes
  (`gSL7OID8LpKvcKo2wIFa`) or "24 hours / 60 minutes" (`z0Sh1ddK10sc4CQ8CDO9`)
  instead of 25 hours; a child standard mass of 38 kg (`cYFtA9PN9tArZbXB9XOp`).
- **Definitions keyed to a different instrument's answer.** Rate of turn keyed as
  a speed (`hO5yAbvOW9XgmaKbxjdK`); VLO keyed to the VLE definition
  (`gE08YBhlw1nlevYNo7wJ`) and VFE to the take-off setting (`rHqw2ek2Dh7qogQ831Xn`);
  the radio altimeter keyed as measuring altitude rather than height
  (`yNB3rNugtcEGT8lFH89m`); the PFD keyed as a systems display
  (`x0N3zoIuy1V9liSq1Fxw`); an EGT limit bug keyed as a vibration indicator
  (`tle55uQrUWrNxtKFzMGr`); the yaw damper keyed as an elevator augmentor
  (`l5goYnVGfOyaSp3JBlah`); the autopilot keyed as performing navigation
  (`ZPxvTndA8vLxhaTBgEGe`).

---

## Bank-wide data defects

### Image-dependent questions — 16 total, 11 in sections NOT yet audited

Unanswerable in a text-only app. **Decide the policy before starting navigation**,
since that section alone has 8:

| Section | Count | Status |
|---|---|---|
| navigation | 8 | audited 2026-08-31 |
| aircraft_general | 3 | not yet audited |
| operational_procedures | 2 | audited — `1JqXlrk6F9uuVMmsq9yN`, `lgkzG8zXAxhwObQHPSgU` |
| principles_of_flight | 3 | audited — `j3vbgqBvVbKVmr7aHwfp`, `rnYhFaq0tNdQBTlXCHaw`, `zfulgMmT5Rur03N123kZ` |

The ids in the section still to audit:

- **aircraft_general (3):** `3BRrS0xVDdbE1LyU3o69`, `3cONw9JS8W4plsQ1DqUz`,
  `yn3S4vqpIsOaF2TOteIr` — hydraulic system schematics

The eight navigation ones (`8iQwwWLJSrMANOeykfyH`, `bC1yhOAegB6kcz4t7o35`,
`NcSKwheZNLCsDGITHTl0`, `OcGbsvFhze9mczc5YcFz`, `qKqKf8oje4qHHCZMw5Xy`,
`rVsdNMQ2p5ukMRrJO6CU`, `u5DiiqacblfuRYpmhG48`, `z08mQj8E0h402yYbv4du`) now carry
concept-only explanations teaching the method — almanac twilight interpolation,
the geocentric/geodetic latitude distinction, the cocked-hat rule for a three-VOR
fix, and the 1-in-60 fix-to-destination procedure. `u5DiiqacblfuRYpmhG48` turned
out to be fully solvable from the stem once a plausible chart variation is
assumed, and its explanation works the whole problem.

For the audited ones, explanations teach the underlying concept instead of
answering the missing diagram. Note that one of the two in operational_procedures
(`lgkzG8zXAxhwObQHPSgU`, a cabin differential-pressure problem) is fully solvable
from the numbers in the stem, so the image is decorative — check that before
writing a concept-only explanation. The navigation ones are the least salvageable:
several require reading values off an almanac extract.

Regenerate the list with:

```bash
python3 -c "
import csv, re
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
for r in rows[1:]:
    if re.search(r'\bimage\s*\d+|refer to image', r[1], re.I): print(r[7], r[0], r[1][:70])
"
```

### Corrupted rows

Parsing defects that leave the question unanswerable as displayed. All have
explanations that teach the underlying point and say the row is corrupted.

- `1QEbIkZTVCVwW26Dcl7c` and `GqaWkUInjL6nWZIuUY1i` (operational_procedures) —
  the tail of the stem has been parsed into the option list. Both are the
  "which extinguisher for a class A fire" question; the intact sibling
  `qDiwJtdgs0Pmh6jRqmMS` keys all four agents.
- `V1xbLmjRKUJbJk6UtzeJ` (operational_procedures) — the text of two *later*
  questions has been appended to option D. The key was corrected to D anyway,
  because that option does begin with the right combination.
- `kk5QTVvNJS99oVtL1FzT` (operational_procedures) — one answer split across
  options C and D.
- `eC7EcJlARNi2lkStFbWR` (meteorology) — question text collapsed into option B.
  The key is still correct.
- `bkRS48ec8ZdBuqRQFQrE` (navigation) — the tail of the stem is parsed into the
  option list, and the marked answer WAS one of those fragments. Reconstructed and
  corrected to "2 dots fly right".
- `KTbspAuuS8D8358cFZR0` (navigation) — the first sentence of the stem is missing,
  leaving only one of the two relative bearings. Intact sibling
  `0fFKwD6ItMxelvePDRA6`; the key is correct.
- `QLHgbZtJqn2KpH55Sm8x` (navigation) — the stem is reduced to "Identify the
  latitude?" with no data at all. Unanswerable; key left.
- `tNfFndlEXhMerkE5ovAH` (navigation) — three separate questions concatenated into
  one row, all referring to aerodrome reports that are not present.
- `hf2kSCw2kKBsXqpSwzyT` (navigation) — stem begins "033-129.htm" and asks for a
  TAF value from a document that is not present.
- `ajjw6bxQSqvA8bhUFELK` and `iEdh1jvTiPRqkx9jlisG` (navigation) — both ask at what
  latitude earth convergency is correctly represented "on the chart", but no chart
  and no cone constant is given anywhere in the stem.
- `TwqYDm0ejZGvbqulQ2cj` (navigation) — asks for the angle between two meridians
  "on the chart" without saying which chart.
- `piaRTAv5jcUwfK0XCsSH` (navigation) — climb-distance question with no starting
  altitude; from sea level the answer is 6.7 NM, which is not offered.
- `zM6HI2SEoXsuLH3wCUgF` (navigation) — longitude printed as 123 deg 75 min, which
  is not a valid coordinate. Reading it as 123.75W gives the keyed answer.
- `uIKGhTk3zXoef0Fic8fi` (human_performance) — the stem contains the whole of a
  *different* question plus the bank's own export metadata ("41740.2.1.2
  Respiratory and circulatory systems Typ: MC 460 AviaExam6931 9/4/1996") before
  the real question. The key is correct for the question that actually follows.
- `iDFU3dX9P5XVSqZUY3CH` (human_performance) — a multi-statement item whose
  options have been flattened into the individual statements ("-2: varies between
  5 and 15%", "-3: may cause dehydration…"), so two of the four are true. The
  marked answer was the one false statement (40-60% humidity) and was corrected to
  the 5-15% figure; the row is still not properly answerable.
- `AmMXyniYBTiH7z7VLJY2` (human_performance) — statement 3 reads "smokers have a
  greater chance of *decreasing* lung cancer". Read as "developing", which the key
  assumes.
- `bBNGDqMcUXkqlGQ6kRyG` (human_performance) — every option denies that
  confirmation bias is common; option A ("not usual") is almost certainly a
  corruption of "not unusual", which is what the key requires.
- `SlEVXAovPbAj3QnUD0Fz` (human_performance) — the stem's tail has migrated into
  option C, which reads "pilot may: A get colour blindness…". Key still correct.
- `kPEEnZmhSCOt7K1L0DCs` (human_performance) — stem garbled to "an aircraft system
  should at The lowest permissible to". Intact siblings `H70JoPZh18pVicz6Oiv1` and
  `DpHMmqEeWjOn0A8eVBZ9`; the key is correct.

- `V1xbLmjRKUJbJk6UtzeJ` (operational_procedures) — option D contains two whole
  unrelated exam questions ("119. The tip vortices…", "120. …noise abatement
  take-off and climb procedure B…") spilled into the field. The intended option D
  ("1, 2, 3, 4, 5") is the first fragment of it, and the key is correct for that.
- `wmaCG1t6BU3CMgGoNKIu` (aircraft_general) — stem and options split across the
  wrong fields: the stem breaks off at "turns left onto" and option C reads
  "should be initiated on a compass heading off:". Not answerable as printed; the
  explanation covers the underlying southern-hemisphere turning error instead.
- `yn3S4vqpIsOaF2TOteIr` (aircraft_general) — refers to "Image I", a hydraulic
  press diagram that is not in the bank. Key left alone; the explanation gives
  Pascal's principle so the item is at least instructive. Add it to the
  image-dependent list above.

### Duplicate or defective option lists

- `Ge8FlHinKPARXKWnuwa9` — "4.0 NM" offered twice.
- `jzK0FSIdQr7cICCj1DVX` — "plus 30 %" offered twice.
- `cRNpc0Z553l1fpIESIL3` and `qDGxwjjRMYg0gHwpmB6T` — ask for the NAT air-to-air
  VHF frequency but omit 123.45 MHz from the options. Both were corrected to
  131.8 MHz (the NAT air-to-air frequency, and in the second case the only VHF
  option at all — the marked answer was 243 MHz, which is UHF).
- Principles of flight: `DRe8FO6lQv1gcJJrRC5i`, `dtDceDNaEyUSKKiepmFh`,
  `sYTdsagu5TcuY8yF9pNy`, `AxewD2T5EYHgSu9TBXdx`, `GoSpYoy3q12S1LTFZ1N6`.
- `QucllHeHu0FD71T2PAt7` (navigation) — options A and D are the same sentence with
  "decision point" changed to "decision airport". Corrected to A, the correct term.
- `kJxsbAjeBxXeU61t1Tne` (navigation) — two options both describe genuine contents
  of the GPS navigation message. Key left.
- `Hrd09rAYTD7dRXpY1hCD` (navigation) — under RVSM both FL300 (keyed) and FL320
  satisfy the semi-circular rule for a magnetic course of 200. Key left.
- `Jg8dcLFFuBMshuBZH013` (human_performance) — "the two types of fatigue" offered
  as "Chronic short-term and acute". Garbled, but the only option naming both
  acute and chronic. Key left.
- `wCLaBpE8aNfXu8EvwWDm` (human_performance) — the true pair (1 and 3) is not
  offered; every combination includes a false statement. Keyed 1,3,4, the nearest.
- `o3J40xxzqyQ4k0YqTyLE` (human_performance) — the true set (1,2,3) is not offered.
  Statement 4 says personality matters "above all", which overstates it, but the
  only alternative combinations drop statements that are plainly true. Key left.
- `UIkAWBwwCb5dHE0aLFmK` (human_performance) — asks how age affects performance,
  but every option is wrong in some respect; the keyed one ("better when relaxed,
  independent of the period of day") ignores the circadian rhythm the rest of the
  syllabus insists on. Key left as the least wrong.
- `wEO0HsuKNZzcIrg0LZDS` (human_performance) — asks what hyperventilation *is*, but
  does not offer "increased lung ventilation" (which `VRUTW0a5I0YbxVkpYpi0` keys).
  Keyed to the compensatory hyperventilation of altitude, which is a real
  phenomenon but not the definition. Key left.

**Aircraft general**

- `T5EIhBf90gswHLwqukJ4` — options A ("operating mass plus passengers and cargo")
  and D ("operating mass plus load of passengers and cargo") say the same thing.
- `RdeKyRPzpOtCiHHb23Ja` — options A and C are printed identically.
- `YbIpm9kZd2yND3K5KNUv` — asks what SAT is; every option pairs the wrong
  descriptor with the wrong unit ("absolute … in degrees Celsius", "relative … in
  degrees Kelvin"). Left keyed D as the least wrong.
- `hJR1ph3A7w5bSzPPyiur` — accessory gearbox drives. The correct set is 2,4,5,6,7
  (N2 tacho, generator and CSD, oil, hydraulic and HP fuel pumps); the keyed
  option has the N1 tacho instead of the N2, and the only alternative drops the
  HP fuel pump. No option is right.
- `utEdjYwgvStDaJjL2pd0` — 10 seats requires one extinguisher on the flight deck
  and one in the cabin; no option offers 1 + 1.
- `UPM8T8ktFjv818s4sGZk` — cabin air source. The bank's `DpZREhevvel8O0T3cFCN`
  and `zUWkuFDIP8WOvorsYIc9` both key "LP, and HP if necessary", which is right,
  but that combination is not among this question's options.
- `kV6YlGSgSZt23koB1A5W` — induction tachometer advantages. Statements 1, 2 and 3
  are all true and no option offers them together.
- `tUZJRjIN2DX2W5OsML38` — electrical protections. Over-speed and under-frequency
  are the same fault expressed two ways, and no option is cleanly right.

### Blank option D — 69 total

54 in air_law, 15 in principles_of_flight, **none in the unaudited sections**.
Keys are still correct; the missing option simply reduces these to three-choice
questions. The 15 in principles_of_flight are `0QW52e6aikKs75YMlzZ7`,
`5JPWZQzo3Imn5DumSd69`, `aswNozoQZobBagf2ZIEt`, `aMoNPwTEIAQfUhHSlpN9`,
`FAaFlFz1n6iWk35PFVGm`, `edC3pMBWHPOdVNoOg5wI`, `I1JP7okftWTqVbCP8vi6`,
`IJPzezETUeluzJG8PuTu`, `oXCsHYZmF1ohIhHcVXXl`, `LO0xR8OMQiFM7aqfu7HS`,
`PDTyq5GvaLBX1urMgzhZ`, `RIGcRcHBHx80Xo5LoVZp`, `RStQvodvqTKmspotjqi2`,
`wnhLuYN6ji8fqOJDxdRm`, `ZAQmC41HBQ6WSoUxi20D`.

---

## Open questions — left as keyed, need a decision

These were NOT changed, because a correction would have been a guess rather than
an audit.

**Operational procedures**

- **Noise abatement procedure A vs B, close-in vs distant relief.**
  `5ifkE0NQGmSvfFmZGYHv` and `eVDMWo3CqoLI8XGTxnTn` both key procedure B for
  relief close to the aerodrome. That matches the older Doc 8168 A/B text the
  bank uses (B reduces power and accelerates earlier) but is the reverse of the
  modern NADP 1 / NADP 2 description. Both keys were left; an earlier correction
  of the first one was reverted for this reason. The explanations flag the clash.
- `nzBkH1n2PinvDO3OXiqj` — lowest height for a power reduction in a noise climb,
  keyed 600 m (2000 ft). The current PANS-OPS floor is 800 ft and the old
  procedure-A figure is 1500 ft; neither is offered.
- `IJe1tEXQrvtEBrHfg2Jy` — hydroplaning speed "if applying brakes", keyed 129 kt,
  which is the rotating-tyre value (9√P). The locked-wheel figure (7.7√P ≈ 110 kt)
  is not among the options.
- **`LUuIJR1VqfENw7fsBaMw` vs `YdUvJhJDPyoSJdyfytie`** — when VFR public transport
  in a single-pilot-certificated aeroplane needs a second pilot. One keys "if the
  flight lasts more than 3 hours", the other "never". One of them is wrong; the
  3-hour figure appears in no JAR-OPS provision, but it may be a national rule.
- `rM7NsJ7fQjflVP8yUNn8` and `wUf3exHYd9AmPOwl6HW8` — a toilet fire fought with
  "all available extinguishers simultaneously". Both key it, so the bank is
  self-consistent, but the phrasing is unusual.
- `8dDvVaYRk436B1j8JLng` — statements 3 and 4 express the same rule two ways, so
  two combinations read as correct.
- `uPO5d949QbIUDiZ5Ki3m` (ceiling IS part of non-precision aerodrome operating
  minima) vs `kGotruKl2kLwBBULwdTV` and `V7AxGWvPTLLyr1gpOOp2` (ceiling does NOT
  govern the approach). Both are defensible — the first asks what the minima
  consist of, the others what controls the approach.
- `YcRS0sPkKtqE4gPg61LL` (hold) vs `8SCaFuWT7xuRRwLuzsNh` (continue on the flight
  plan) for an oceanic clearance not received by the boundary. The second stem
  says "except Shanwick Oceanic", which is exactly the case where holding is
  required, so the pair is consistent — but only if you read the stems closely.
- `LQ2XBqCZZXXnV9Hw3gD9` — certificate of airworthiness validity, a national rule
  the bank states as three years. Not verifiable against JAR-OPS.

**Principles of flight**

- **Two near-identical questions keyed to DIFFERENT options, both statements
  true:** `mmHD0wsMTVbEBoS08iJp` vs `Ung19E8mcWDOZTmkTxAW` (servo tab), and
  `kk2YmZMPKtvE3W7c4Dln` vs `ZQLcmwSXw1CZzn9y1aHs` (elevator position when
  trimmed).
- **Flap pitching moment answered both ways:** `p0QNEqMVkZWjuugYQ84W` (nose up)
  vs `jbhdZXNVlcoRAvqQOz2w` (nose down, Fowler).
- `mK7ENZlFBoo26QgZ3SLW` asks for maximum glide TIME but offers only minimum-drag
  and minimum-glide-angle options.

**Navigation**

- `9bz83kchIdN3V1Tu1Cvr` — minimum fuel for a VFR flight to an offshore platform,
  keyed "identical to VFR over land". Several offshore regimes require IFR-standard
  fuel because a platform has no usable VFR alternate. Not verifiable against the
  bank's own frame of reference.
- `AkgLaWzBUpfia1ssBT9L` — 5000 ft on QFE 963, second altimeter on 1013. The exact
  standard-atmosphere answer is 6400 ft, EXACTLY midway between the two offered
  values 6300 and 6500. Keyed 6500.
- `GXG3SBZNY5yn5HJluzaz` and `KyFHgl8azJ2Fc1GdYiDb` — PET with an "equivalent
  headwind" of -20 out and +40 back. Read literally (-20 headwind = 20 kt tailwind)
  the answer is 470 NM; read as signed components it is 530 NM. Both are offered.
  BOTH siblings key 530, so the bank is self-consistent and the keys were left.
- `tjwHwIKixKZa5njoNhay` — W/V works out at 073/45; the options offer 070/45 and
  075/45 and 073 sits between them. Keyed 070/45.
- `9SddSp6uNpiDpRTyl4ZN` — reverse-track wind component computes to -61 kt against
  offered values of -55 and -65. Keyed -65, the nearer.
- `4Wy9GNlC1CbCm24UMgqr` — MLS advantage over ILS. "Integral DME/P so no separate
  range aid" (keyed) and "insensitive to geographical site" are both standard
  textbook advantages.
- `MAE6JXZhVlWKHd01pCLt` — B737-400 indication when radio updating is lost. The
  stem's "if any" phrasing and the invented display name "MFDU" in the keyed option
  both suggest the answer should be "no indication while the IRS positions remain
  within limits", but this is aircraft-specific detail that could not be verified.
- `zgJGTKTl4iN7rNXhTCN7` — the keyed rhumb line distance of 578 NM should be
  648.7 x cos30 = 562 NM. The other three options are demonstrably wrong (full
  convergency instead of half, wrong quadrant, dlong in minutes labelled NM), so
  the key was left as the only option of the right kind.
- `xBpsdy1LJbBZnY6oVXJH` — whether a VOR designating an RNAV waypoint must be in
  range when ENTERED or only when USED. Keyed "must be in range".

**Human performance**

- **"Which of these provides the basis of all perceptions?"** — `5ci2hY850YfmWa3uC11H`
  and `GX4vyEWXEyaPxJHOxgA7` both key "the intensity of the stimuli". The Gestalt
  answer, and the one most HPL texts give, is "the separation of figure and
  background", which both items offer. Two entries keyed the same way is one source
  question duplicated, not independent corroboration — but the key was left rather
  than overturn a self-consistent bank on a textbook-phrasing argument.
- **Hypoxia critical threshold, three different figures.** `iClF3YKh5YsP1MvxaiAp`
  keys 20,000 ft, `2zEnhsolg65gLhziZmQc` keys 18,000 ft, and `MkzrKBlleC5NUM2eKCIk`
  keyed 38,000 ft. The last was corrected to 22,000 ft (the only offered value
  inside the critical zone); the 18,000 / 20,000 clash is unresolved.
- **Rasmussen error types, rule-based mode.** `8HqpsotTzFitY2pyGPLj` keys "errors of
  technical knowledge" and `JeT4jVcyMbqgbCkXaxZU` keyed "handling errors" — the same
  question with the options shuffled. `gH2wPX3XT1z3Le1eHO2d` fixes skill-based =
  routine errors and knowledge-based = creative errors, leaving the other two both
  claiming the rule-based slot. JeT4 was corrected to match 8Hqp on the argument
  that handling errors are execution slips, but the bank's taxonomy is not
  self-consistent here.
- `psnHuOlMK7Ha5L4sFApX` — the DECIDE model keyed as "a prescriptive generic model
  which is subject to mathematical logic". DECIDE is prescriptive but heuristic, not
  mathematical, and the option immediately above it defines the mathematical model
  as *normative*. Left as keyed; no sibling to settle it.
- `uGnnX0a2VbudIkwPJvnZ` — when pilots take greater risks, keyed "making decisions
  independently of others". The risky-shift and audience literature both point at
  the other option (part of a group, feeling observed and admired, e.g. air shows).
  Both are defensible; key left.
- `oClSCOcE0ajSge4GKjtW` — detectability keyed as including "tolerance of the various
  systems to errors", which is a different property from detection. "2 and 4" alone
  reads better; key left.
- `4LtznRYTQrSsXyDTWsQl` ("skill and/or rule based") vs `nfFglUn1AkXqiRu2xExq`
  (skill-based) vs the same stem elsewhere — when to select flaps. The bank itself
  says "and/or" in one place, so the single-answer variants cannot be settled.
- `bgzCyxPkMMJCVa4JRwKt` — how long to wait after an uneventful rapid decompression,
  keyed 12 hours. `dgkjjfDf0CBDHPNkvcIX` keys "seek prompt aeromedical advice" for
  the same scenario. 12 h is not corroborated anywhere else in the bank.
- `wAsBfc0ihziVPLORnkVu` (includes the gastrointestinal system) vs
  `T6w6opzwN3yXQjZbDjHl` (excludes it) — systems involved in motion sickness. The
  stems differ slightly ("involved in" vs "involved in the appearance of"), so both
  keys can stand.
- `oemS0oxqDwotGiItNM0c` (8,000 ft) vs `sI3L3D4Q2RZNTZxVyysS` (12,000 ft) — the
  altitude at which short-term memory is first affected. Different option sets, and
  8,000 is not offered in the second; both keys left.
- `kFATYwS38cBqUC82FjkM` / `pEHmiU0gDrOhnxGI2k6w` — confirmation bias defined as
  "ignoring information indicating the decision is poor" in one and as "looking for
  facts that confirm expectations" in the other. Both describe it; both keys left.
- `mT9P9RNxfA3y007MwWRy` — the resistance phase attributed to "the parasympathetic
  system" using cortisol. Cortisol is an HPA-axis hormone, not parasympathetic, but
  the rest of the statement set is correct and no alternative combination works.

**Aircraft general**

Conflicting sibling pairs — both keys left, one of each pair is wrong:

- `B7QlGhIOZ8Zu3jL3UfXh` (both valves closed) vs `iVKrOAUyOUI21V1ml3Qz` (corrected
  to exhaust open, inlet closed) — valve positions at the end of the power stroke.
  The first is the ideal-cycle answer, the second the real-timing answer; the
  stems differ only by "so as to get the optimum efficiency".
- `jxiwLZIPxcNBIiwS1x2Z` vs `scOovxMzQI0ryJ59KXNY` — density altitude. One keys
  "pressure altitude corrected for relative density", the other "the altitude in
  the standard atmosphere at which the density equals the actual density". Both
  definitions are correct; the option sets are identical.
- `gv3OTJbjGrU4OX8PP1qJ` (3,4) vs `tfFahf1qeE6TEZNDffbb` (1,3) — what a rate of
  turn indicator shows. The disagreement is whether it indicates the yaw rate or
  the rate of turn about the true vertical; in a banked turn these differ.
- `dXGwG4pGDkCSalid1Aze` (defective condenser) vs `ybvjth31cOCnP0hYDCp8`
  (excessive carbon) — why an engine will not stop with the mags off. The real
  answer, an open P-lead, is not offered in either.
- `crxhVLZkMKiIYCTY4MM3` / `VQHqYC2t4RhGI9PGbGuK` ("high pressure and large flow")
  vs `z83aPXKuajtvJMuBzc2Z` ("high pressure and low volume flow"). The stems ask
  slightly different things — what the system produces, and what gives maximum
  power for least mass — but the pair reads as a contradiction.
- `P86N87WRaMh0QIpCaiY8` vs `QfZJrnVgIQktx4wIVFyY` (rain protection);
  `Q7Wu55bNqjOmDpMmbR21` vs `RDBEK5xiVBMjtOZRWDKt` (tubeless tyre characteristics).

Single items left as keyed, with the doubt recorded:

- `kYIZ93cBxnCNef2nX5GK` — the FMS defined as a "global 2-D" management system.
  An FMS with VNAV manages the vertical profile too, so 3-D looks right, but
  nothing in the bank settles it and no sibling exists.
- `mvkizXykyOIwAvoUIHMp` — passenger masks to deploy before the cabin altitude
  exceeds 12 000 ft. CS 25.1447 says 15 000 ft and most question banks say
  14 000; the keyed figure matches neither, but there is no way to choose between
  the two candidates from the options given.
- `lC2gHyEvWL8dEaJY4S8E` — corrected to the electric unfeathering pump on the
  strength of `ppbyNP3z3bnbAtgaIquy`. Worth a second opinion: some light twins do
  unfeather aerodynamically once the blades are off the stop.
- `gdBsBPqnNFZWiMosLPGJ` — heading gyro of a "three-axis data generator" given as
  1 degree of freedom with a *vertical* spin axis. A conventional DG has two
  degrees of freedom and a horizontal axis; the phrasing may describe a rate-gyro
  package, so the key was left.
- `iOSt3RT8A1tBIpHyRwTd` — gaseous fire loop tested "by heating up the sensor".
  Plausible in principle, impractical for a three-metre loop in flight.
- `xr3hFi77jQOoT0jm7IOc` — air cycle machine. The keyed option (compressor raises
  the temperature, improving the secondary exchanger) and option D (the turbine's
  temperature drop is the main cooling effect) are both true statements.
- `woToCw3Q6cqMrDAun7Jj` — capacitance gauge "information independent of fuel
  temperature". True, but option D says the same thing more completely.
- `qJORP2sjXm0tOJ05M0Xl`, `trn3SSEUYKbwUqJRD6vo` — helicopter DOM and empty mass.
  The option wording does not match the standard definitions closely enough to
  correct with confidence.
- `zw47YghLH2RbwlhZux6M` — which terms drive the autopilot's altitude-hold
  correction; the keyed 1,2,3 excludes the altitude-error rate term, which a real
  control law would use.
- `wOI7rArKKH8bX3WP6Tl2` — a single autopilot called "fail soft"; "fail passive"
  is arguably the better term for a simplex channel that simply disconnects.

**Air law**

- `0lfw6se01Qq66VP5fwqq` stays B (MET) — correct as framed against the legacy AIP
  structure. Check the frame of reference before "fixing" an answer.

---

## Build state — REBUILT 2026-09-21

`app/build/outputs/bundle/release/app-release.aab` was rebuilt on 2026-09-21 from
the final TSV (all seven sections audited, `TSV_ASSET_VERSION = 7`). This is the
build to upload. If the TSV or any source changes again, rebuild:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-23.jdk/Contents/Home
./gradlew :app:bundleRelease
```

**JDK:** Gradle 8.14.5 refuses JDK 25 (Android Studio's bundled JBR and the
`java` on PATH are both 25). Temurin 23 is what `.idea/gradle.xml` uses;
Corretto 21 and JBR 17 under `~/Library/Java/JavaVirtualMachines` also work.

**versionCode 10 / 1.2.1** (bumped 2026-09-21: Play already holds a version
code 9 from the stale 24 Aug upload and refused the new bundle as a duplicate;
the user saw "You cannot remove all production APKs and Android App Bundles"
after deleting the old one from an empty release). targetSdk 36, Play
Billing 9.1.0 — both confirmed in
the packaged release manifest and the resolved dependency cache, not just the
source. Clears both 31 Aug 2026 Play deadlines (billing >= 8.0.0, targetSdk 36).

Release notes and store listing copy: `docs/STORE_LISTING.md` (logbook lines
removed 2026-09-21 because the feature is hidden).

### TSV version stamp

`QuestionCacheManager.kt` compares an integer `tsv_data_version` against
`TSV_ASSET_VERSION`, **now 7** (bumped 2026-09-09 for the aircraft-general pass,
which completed the bank). Version 6 covered human performance, version 5
navigation, version 4 operational procedures, version 3 meteorology and
principles of flight.

**Bump `TSV_ASSET_VERSION` whenever the TSV changes** — existing users re-parse
the TSV on next launch when the stored version differs.

---

## Git on this machine — use Homebrew git (2026-09-21)

`/usr/bin/git` fails because `xcode-select` has no active developer directory,
and the `~/Downloads/Xcode.app` copy that earlier sessions used has been deleted.
**`/opt/homebrew/bin/git` (2.55.0) was installed on 2026-09-21 and works.** Put
`/opt/homebrew/bin` ahead of `/usr/bin` on PATH, or call it by full path.

**Pushing:** `gh` now holds two accounts; `suyashm002` (the repo owner) is the
active one and is what pushes work. Push with the CLI's credential helper so
the keychain's missing entry does not matter:

```bash
/opt/homebrew/bin/git -c credential.helper='!gh auth git-credential' push
```

`release/1.2.0-play-compliance` was pushed on 2026-09-21 and tracks
`origin/release/1.2.0-play-compliance`. Nothing is local-only any more.

## Upgrade path — VERIFIED 2026-09-21 on an emulator

The Room v4 → v5 → v6 migrations were exercised end to end on
`Pixel_4_API_Tiramisu`:

1. `main` (versionCode 1, Room v4 — the oldest schema in the repo) was rebuilt
   as a debug APK from the git objects and installed. It created the v4 database
   and parsed all 7,948 questions.
2. A logbook row was inserted into `flight_entries` (pulled with `run-as`,
   edited with Python's sqlite3, pushed back) so the migration had real user
   data to carry.
3. The current debug build (versionCode 9, Room v6) was installed with
   `adb install -r` and launched.

Result: no crash, `PRAGMA user_version` went 4 → 6, `question_feedback` was
created, all nine new `flight_entries` columns exist, the test row survived with
`blockTime` seeded from `totalFlightTime` (1.5 → 1.5) as `MIGRATION_5_6`
intends, and the questions reloaded (`stored=0, current=7`) with 7,945
explanations present.

Not exercised: the `claimLegacyEntries` userId re-homing, which only runs when a
user is signed in (the emulator was not). It is a single `UPDATE ... WHERE
userId = 'current_user'` and cannot drop rows.

Caveat: the live 1.1.1 Play build's source is not in the repo, so this proves
the migration chain from the oldest schema *in the repo*, not from the exact
bytes on users' devices. A staged rollout of 10–20% with the crash dashboard
open is still cheap insurance. Both debug builds were signed with the debug key;
signing does not affect Room.

The scratch copy of `main` used for this lives only in the session scratchpad
and is not part of the repo. To repeat it: extract `main` with Python from
`.git/objects`, copy `local.properties`, `app/google-services.json` and the TSV
in, write a `gradle.properties` with `android.useAndroidX=true`, point it at the
current `libs.versions.toml` and Gradle wrapper (the old AGP/Kotlin/Gradle are
not cached and the network is slow), set `compileSdk = 36`, and drop in the
current `BillingManager.kt` (billing 9 changed one callback signature).

---

## Question bank facts

7,948 questions in `app/src/main/assets/all_questions.tsv`, 10 tab-separated
columns. (Earlier notes claiming 9,076 were wrong — verified by row count and
device logs.)

Column 10 is `explanation`, rendered by `ResultsScreen.kt` when non-blank.
7,945 rows carry one (3 are intentionally blank). Verified 2026-09-21.

**Do NOT use an LLM API for explanations — Claude writes them directly
in-session.**

All corrections are auditable in `tools/answer_corrections.json` with a written
reason and a `from` value that is verified before writing. Most operational
procedures, navigation and human performance entries also cite the sibling
question id that proves the correction.

---

## Environment

- Build from the CLI only. Android Studio is Ladybug 2024.2 and cannot open
  AGP 8.13.2 — that is an IDE sync limit, not a build failure. Studio needs
  2025.2 (Otter) or newer; the download page misdetects macOS 26.5 arm64, so use
  "Download options" → Mac (Apple silicon), or `brew install --cask android-studio`.
- Test device: Samsung SM-A146B (Android 15), connected via adb.
  `adb install -r app/build/outputs/apk/release/app-release.apk`
- Emulators exist too: `Pixel_4_API_Tiramisu`, `Pixel_Tablet_API_Tiramisu`,
  `Nexus_7_2013`. Boot headless with
  `~/Library/Android/sdk/emulator/emulator -avd Pixel_4_API_Tiramisu -no-window -no-audio`.
  They are production images: no `adb root`, no `run-as` on release builds.
- `adb shell pm clear com.suyash.mockcivilaviationexam` wipes the login. Only use
  it when a question-bank reload is needed.
- A GateGuard hook blocks the first Bash command of each context window, and any
  file creation, until a short set of facts is stated. Present them, then re-run
  the same command — it passes on the retry.

---

## Known defects (not blocking 1.2.1)

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

- `tools/explanations_cache.json` — 7,948 explanations
- `tools/answer_corrections.json` — 775 corrections, each with a `from`, `to`
  and a written reason
- `tools/merge_batch.py` — the validating merge helper (see the batch workflow
  above); run it on one batch at a time, never two concurrently

The TSV is a build artifact regenerated from those by `apply_corrections.py` and
`apply_explanations.py`. **If the TSV is ever lost or reverted, re-run both
scripts to rebuild it** — no work is lost. Conversely, editing the TSV directly
would lose the change on the next apply, so always author into the caches.

A fresh clone will not have the TSV at all. Given the DMCA/provenance concern
noted under "Deferred", keeping the copied question text out of the repo is
probably deliberate — worth confirming before changing it.

## Commit state

Committed on `release/1.2.0-play-compliance`:

- 2026-08-26 — the two caches, the `QuestionCacheManager.kt` version-stamp change,
  and the previous handoff.
- 2026-08-31 — the operational-procedures pass (885 explanations, 115 corrections)
  and the navigation pass (1,090 explanations, 133 corrections), plus
  `tools/merge_batch.py`, `TSV_ASSET_VERSION` 3 -> 5, and this file. The 2026-08-30
  operational-procedures work had never been committed because git was unusable
  that day; it went in with this commit.
- 2026-08-31 (later) — `tools/merge_batch.py` hardened after four batch merges were
  silently lost mid-session: an exclusive `flock` held across the whole
  read-modify-write, per-PID temp files, and a read-back verification that exits
  non-zero if any key is not present with the exact value it was given. Merges now
  print "read-back verified". **Never run two merges concurrently anyway.**
- 2026-09-08 — the human-performance pass (1,417 explanations, 89 corrections),
  `TSV_ASSET_VERSION` 5 -> 6, and this file.
- 2026-09-09 — the aircraft-general pass (2,559 explanations, 162 corrections),
  which finishes the bank at 7,948/7,948 explanations and 775 corrections;
  `TSV_ASSET_VERSION` 6 -> 7, and this file.

- 2026-09-21 — release readiness: store listing without the logbook, this
  file, the MainActivity comment; and, separately, the Gradle wrapper
  8.14.3 -> 8.14.5 bump (the version the release bundle was built with).

Nothing has been pushed.
