#!/usr/bin/env python3
"""Validate a batch of explanations/corrections, then merge into the caches.

Usage: python3 tools/merge_batch.py <batch.json>

Batch format:
  {"explanations": {id: text, ...},
   "corrections":  {id: {"from": "A", "to": "B", "reason": "..."}, ...}}

Refuses the WHOLE batch on any validation failure, so a mis-typed id cannot
silently do nothing.
"""
import csv, json, sys, os

TSV = 'app/src/main/assets/all_questions.tsv'
EX = 'tools/explanations_cache.json'
CO = 'tools/answer_corrections.json'

def main(path):
    rows = list(csv.reader(open(TSV, encoding='utf-8'), delimiter='\t'))
    hdr = rows[0]
    ai = hdr.index('correctAnswer')
    by_id = {r[0]: r for r in rows[1:]}

    batch = json.load(open(path, encoding='utf-8'))
    exps = batch.get('explanations', {})
    cors = batch.get('corrections', {})

    ex_cache = json.load(open(EX, encoding='utf-8'))
    co_cache = json.load(open(CO, encoding='utf-8'))

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
            # already-applied corrections are fine if they match the cache
            if qid in co_cache and co_cache[qid]['to'] == cur and co_cache[qid]['to'] == to:
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

    for target, data in ((EX, ex_cache), (CO, co_cache)):
        tmp = target + '.tmp'
        with open(tmp, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2, sort_keys=True)
        os.replace(tmp, target)

    print(f"OK — explanations +{new_ex} (total {len(ex_cache)}), "
          f"corrections +{new_co} (total {len(co_cache)})")
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv[1]))
