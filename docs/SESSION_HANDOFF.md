# Handoff — Aviation Exam Pro

State as of 2026-08-31. Read this first in a new session.

---

## START HERE — next action

The question-bank audit is 63% done (5 of 7 sections). Next section is
**human_performance** (1,417 questions).

All shell/python snippets in this file assume the repo root as the working
directory. Each has been run and verified as written.

```bash
# 1. Dump the section to a working file
python3 - <<'EOF'
import csv
rows = list(csv.reader(open('app/src/main/assets/all_questions.tsv', encoding='utf-8'), delimiter='\t'))
h = rows[0]; i = {k: n for n, k in enumerate(h)}
n = 0
with open('/tmp/hp.txt', 'w', encoding='utf-8') as f:
    for r in rows[1:]:
        if r[i['sectionId']] != 'human_performance': continue
        n += 1
        f.write(f"[{n}] {r[0]}\nQ: {r[1]}\nA) {r[2]}\nB) {r[3]}\nC) {r[4]}\nD) {r[5]}\nKEY: {r[6]}\n\n")
print(n, 'written')
EOF

# 2. Work through it in batches of ~43, authoring each batch as a JSON file
#    and merging it with the validator (see "The batch workflow" below)
python3 tools/merge_batch.py /path/to/batchNN.json

# 3. When the section is finished, apply and bump the version stamp
python3 tools/apply_corrections.py
python3 tools/apply_explanations.py
# then edit TSV_ASSET_VERSION in QuestionCacheManager.kt
```

Read **"How to audit a section"** below before starting — the cross-check
technique there accounted for 63 of the 133 navigation corrections.

---

## Progress

| Section | Questions | Explanations | Corrections | Error rate |
|---|---|---|---|---|
| air_law | 817 | 817 | 114 | 14% |
| principles_of_flight | 671 | 671 | 97 | 14.5% |
| meteorology | 509 | 509 | 65 | 13% |
| operational_procedures | 885 | 885 | 115 | 13% |
| navigation | 1,090 | 1,090 | 133 | 12% |
| **human_performance** | **1,417** | — | — | **next** |
| aircraft_general | 2,559 | — | — | |
| **Total** | **7,948** | **3,972 (50%)** | **524** | |

3,976 questions remain. The wrong-answer rate has now been measured at 12–14.5%
across five independent sections, so expect roughly **520 more bad answers**.

Suggested order: smallest first — human_performance, then aircraft_general.

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

**Air law**

- `0lfw6se01Qq66VP5fwqq` stays B (MET) — correct as framed against the legacy AIP
  structure. Check the frame of reference before "fixing" an answer.

---

## Build state — REBUILD NEEDED before uploading

The AAB from an earlier session is stale: the TSV has changed four times since
(meteorology, principles of flight, operational procedures, navigation) and
`QuestionCacheManager.kt` has changed again.

```
./gradlew :app:bundleRelease
```

versionCode 9 / 1.2.0, targetSdk 36, Play Billing 9.1.0.
Clears both 31 Aug 2026 Play deadlines (billing >= 8.0.0, targetSdk 36).

Release notes and store listing copy: `docs/STORE_LISTING.md`.

### TSV version stamp

`QuestionCacheManager.kt` compares an integer `tsv_data_version` against
`TSV_ASSET_VERSION`, **now 5** (bumped 2026-08-31 for the navigation pass).
Version 4 covered operational procedures, version 3 meteorology and principles of
flight.

**Bump `TSV_ASSET_VERSION` whenever the TSV changes** — existing users re-parse
the TSV on next launch when the stored version differs.

---

## Git on this machine — broken shim, working binary

`/usr/bin/git` still fails before it starts:

```
git: error: unable to locate xcodebuild, please make sure the path to the Xcode folder is set correctly!
```

The cause is that `xcode-select -p` points at `/Users/suyashmishra/Downloads/Xcode.app/Contents/Developer`,
an incomplete Xcode, and `/Library/Developer/CommandLineTools` is present but has
no `usr/bin/xcrun`, so `DEVELOPER_DIR` cannot be used as a workaround either.

**The real git binary inside that Xcode.app works fine** and was used for this
session's commits:

```bash
/Users/suyashmishra/Downloads/Xcode.app/Contents/Developer/usr/bin/git status
```

Set an alias, or fix it properly with one of:

```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer   # if a full Xcode is installed there
xcode-select --install                                                  # reinstall the Command Line Tools
```

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
3,969 rows now carry one (3 more are intentionally blank).

**Do NOT use an LLM API for explanations — Claude writes them directly
in-session.**

All corrections are auditable in `tools/answer_corrections.json` with a written
reason and a `from` value that is verified before writing. Most operational
procedures and navigation entries also cite the sibling question id that proves
the correction.

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
- A GateGuard hook blocks the first Bash command of each context window, and any
  file creation, until a short set of facts is stated. Present them, then re-run
  the same command — it passes on the retry.

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

- `tools/explanations_cache.json` — 3,972 explanations
- `tools/answer_corrections.json` — 524 corrections, each with a `from`, `to`
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

Still uncommitted and unrelated: `gradle/wrapper/gradle-wrapper.properties`
(Gradle 8.14.3 -> 8.14.5, bumped by the wrapper itself). Left alone deliberately —
commit it separately if wanted.

Nothing has been pushed.
