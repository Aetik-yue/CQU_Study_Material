# -*- coding: utf-8 -*-
"""批量对齐检查：遍历 test/reg_*.txt，按块校验行宽一致性"""
import glob
import os
import re
import sys
import unicodedata

sys.stdout.reconfigure(encoding="utf-8")
ansi = re.compile(r"\x1b\[[0-9;?]*[a-zA-Z]")


def width(s):
    w = 0
    for ch in s:
        w += 2 if unicodedata.east_asian_width(ch) in ("W", "F") else 1
    return w


total = 0
failed = 0
for path in sorted(glob.glob("test/reg_*.txt")):
    total += 1
    current = None
    problems = []
    for i, line in enumerate(open(path, encoding="utf-8", errors="replace"), 1):
        line = ansi.sub("", line.rstrip("\r\n"))
        if line.startswith(("  ╭", "  ┌")):
            current = width(line)
        elif line.startswith(("  │", "  ├")) and current is not None:
            if width(line) != current:
                problems.append((i, width(line), current))
        elif line.startswith(("  ╰", "  └")):
            if current is not None and width(line) != current:
                problems.append((i, width(line), current))
            current = None
    if problems:
        failed += 1
        print(f"[FAIL] {os.path.basename(path)}: {problems[:5]}")
    else:
        print(f"[PASS] {os.path.basename(path)}")
print(f"\n{total - failed}/{total} 个输出通过对齐检查")
