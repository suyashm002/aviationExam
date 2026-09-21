#!/usr/bin/env python3
"""
Apply audited answer-key corrections to the bundled question TSV.

Corrections live in tools/answer_corrections.json as
{question_id: {"from": "B", "to": "C", "reason": "..."}}.

The "from" value is verified before writing, so a correction cannot silently
apply twice or land on a row that has already changed underneath it. Changing
an answer key in an exam-prep app is high-consequence, so every change is
recorded with its justification and is reviewable in git.

    python3 tools/apply_corrections.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TSV = ROOT / "app/src/main/assets/all_questions.tsv"
CORRECTIONS = ROOT / "tools/answer_corrections.json"
ANSWER_COL = 6

def main() -> None:
    fixes = json.loads(CORRECTIONS.read_text(encoding="utf-8"))
    lines = TSV.read_text(encoding="utf-8").rstrip("\n").split("\n")
    header, rows = lines[0], [ln.split("\t") for ln in lines[1:]]

    applied, skipped = 0, []
    for row in rows:
        fix = fixes.get(row[0])
        if not fix:
            continue
        if row[ANSWER_COL] != fix["from"]:
            skipped.append(f"{row[0]}: expected '{fix['from']}', found '{row[ANSWER_COL]}'")
            continue
        row[ANSWER_COL] = fix["to"]
        applied += 1

    TSV.write_text("\n".join([header] + ["\t".join(r) for r in rows]) + "\n", encoding="utf-8")
    print(f"{applied} answer(s) corrected")
    for s in skipped:
        print(f"  skipped — {s}")

if __name__ == "__main__":
    main()
