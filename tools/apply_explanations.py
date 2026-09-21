#!/usr/bin/env python3
"""
Merge authored explanations into the bundled question TSV (column 10).

Explanations live in tools/explanations_cache.json as {question_id: text}.
An empty string means "deliberately no explanation" — the app renders nothing
for those, which is the intended behaviour for questions whose answer could not
be verified.

    python3 tools/apply_explanations.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TSV = ROOT / "app/src/main/assets/all_questions.tsv"
CACHE = ROOT / "tools/explanations_cache.json"
COL = 9  # 0-indexed explanation column

def main() -> None:
    cache = json.loads(CACHE.read_text(encoding="utf-8"))
    lines = TSV.read_text(encoding="utf-8").rstrip("\n").split("\n")
    header, rows = lines[0], [ln.split("\t") for ln in lines[1:]]

    head = header.split("\t")
    while len(head) <= COL:
        head.append("explanation" if len(head) == COL else "")

    out, filled = ["\t".join(head)], 0
    for r in rows:
        row = list(r) + [""] * max(0, COL + 1 - len(r))
        text = cache.get(r[0])
        if text:
            # TSV is tab/newline delimited — collapse any whitespace.
            row[COL] = " ".join(text.split())
            filled += 1
        out.append("\t".join(row))

    TSV.write_text("\n".join(out) + "\n", encoding="utf-8")
    blank = sum(1 for v in cache.values() if not v)
    print(f"{filled} explanations written, {blank} intentionally blank, "
          f"{len(rows)} rows total")

if __name__ == "__main__":
    main()
