#!/usr/bin/env python3
"""Validate a batch of explanations/corrections, then merge into the caches.

Usage:
  python3 tools/merge_batch.py <batch.json>          merge one batch
  python3 tools/merge_batch.py --verify <batch.json> check a batch is already in
  python3 tools/merge_batch.py --coverage <section>  count explanations in a section

Batch format:
  {"explanations": {id: text, ...},
   "corrections":  {id: {"from": "A", "to": "B", "reason": "..."}, ...}}

Refuses the WHOLE batch on any validation failure, so a mis-typed id cannot
silently do nothing.

Concurrency: the caches are updated read-modify-write, so two merges running at
once would silently clobber each other (this actually happened and cost four
batches). Three defences now:
  * an exclusive flock held across the whole read-modify-write,
  * a per-process temp file, so two writers cannot share one,
  * a read-back verification after writing - if every key is not present with
    the exact value it was given, the run exits non-zero and says so.
"""
import csv, json, sys, os, fcntl

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TSV = os.path.join(ROOT, 'app/src/main/assets/all_questions.tsv')
EX = os.path.join(ROOT, 'tools/explanations_cache.json')
CO = os.path.join(ROOT, 'tools/answer_corrections.json')
LOCK = os.path.join(ROOT, 'tools/.cache.lock')


def load(path):
    with open(path, encoding='utf-8') as f:
        return json.load(f)


def write_atomic(target, data):
    tmp = f"{target}.tmp.{os.getpid()}"
    with open(tmp, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
        f.flush()
        os.fsync(f.fileno())
    os.replace(tmp, target)


def readback_check(exps, cors):
    """Re-read from disk and confirm the batch really landed."""
    ex, co = load(EX), load(CO)
    lost = [q for q, t in exps.items() if ex.get(q) != t]
    lost += [q for q, c in cors.items() if (co.get(q) or {}).get('to') != c['to']]
    return lost


def coverage(section):
    rows = list(csv.reader(open(TSV, encoding='utf-8'), delimiter='\t'))
    i = rows[0].index('sectionId')
    ex = load(EX)
    sec = [r for r in rows[1:] if r[i] == section]
    have = sum(1 for r in sec if r[0] in ex)
    missing = [r[0] for r in sec if r[0] not in ex]
    print(f"{section}: {have} / {len(sec)} explanations")
    if missing:
        print(f"  MISSING {len(missing)}: {' '.join(missing[:12])}"
              f"{' ...' if len(missing) > 12 else ''}")
    return 0 if not missing else 1


def main(argv):
    if argv[0] == '--coverage':
        return coverage(argv[1])
    verify_only = argv[0] == '--verify'
    path = argv[1] if verify_only else argv[0]

    batch = load(path)
    exps = batch.get('explanations', {})
    cors = batch.get('corrections', {})

    if verify_only:
        lost = readback_check(exps, cors)
        print(f"{os.path.basename(path)}: "
              f"{'OK — all present' if not lost else f'{len(lost)} NOT in cache: ' + ' '.join(lost[:12])}")
        return 0 if not lost else 1

    rows = list(csv.reader(open(TSV, encoding='utf-8'), delimiter='\t'))
    ai = rows[0].index('correctAnswer')
    by_id = {r[0]: r for r in rows[1:]}

    # Hold an exclusive lock across read, modify and write.
    with open(LOCK, 'w') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)

        ex_cache, co_cache = load(EX), load(CO)

        errs = []
        for qid, text in exps.items():
            if qid not in by_id:
                errs.append(f"explanation: unknown id {qid}")
            elif not isinstance(text, str) or not text.strip():
                errs.append(f"explanation: empty text for {qid}")
            elif '\t' in text or '\n' in text:
                errs.append(f"explanation: tab/newline in text for {qid}")

        for qid, c in cors.items():
            if qid not in by_id:
                errs.append(f"correction: unknown id {qid}")
                continue
            frm, to = c.get('from'), c.get('to')
            cur = by_id[qid][ai].strip().upper()
            if frm != cur:
                # an already-applied correction is fine if it matches the cache
                if qid in co_cache and co_cache[qid]['to'] == cur == to:
                    continue
                errs.append(f"correction {qid}: from={frm!r} but TSV holds {cur!r}")
            if to not in ('A', 'B', 'C', 'D'):
                errs.append(f"correction {qid}: to={to!r} not A-D")
            if to == frm:
                errs.append(f"correction {qid}: to == from ({to})")
            if not str(c.get('reason', '')).strip():
                errs.append(f"correction {qid}: no reason given")

        if errs:
            print(f"REFUSED — {len(errs)} problem(s), nothing written:")
            for e in errs:
                print("  -", e)
            return 1

        new_ex = sum(1 for k in exps if k not in ex_cache)
        new_co = sum(1 for k in cors if k not in co_cache)
        ex_cache.update(exps)
        co_cache.update(cors)
        write_atomic(EX, ex_cache)
        write_atomic(CO, co_cache)

        lost = readback_check(exps, cors)
        if lost:
            print(f"FAILED — wrote, but {len(lost)} entries are not on disk "
                  f"afterwards (concurrent write?): {' '.join(lost[:12])}")
            return 1

        print(f"OK — explanations +{new_ex} (total {len(ex_cache)}), "
              f"corrections +{new_co} (total {len(co_cache)}) — read-back verified")
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
