# -*- coding: utf-8 -*-
"""界面渲染检查：
1) 按"块"解析（╭…╰ 为面板块、┌…└ 为表格块），校验块内每行显示宽度一致
2) preview 模式输出去 ANSI 的纯文本预览
"""
import re
import sys
import unicodedata

sys.stdout.reconfigure(encoding="utf-8")
path = sys.argv[1]
mode = sys.argv[2] if len(sys.argv) > 2 else "check"

ansi = re.compile(r"\x1b\[[0-9;?]*[a-zA-Z]")


def width(s):
    w = 0
    for ch in s:
        if unicodedata.east_asian_width(ch) in ("W", "F"):
            w += 2
        else:
            w += 1
    return w


lines = [ansi.sub("", line.rstrip("\r\n")) for line in open(path, encoding="utf-8", errors="replace")]

if mode == "preview":
    print("\n".join(lines))
    sys.exit(0)

blocks = []          # (tag, start_line, [widths...])
current = None
for i, line in enumerate(lines, 1):
    if line.startswith(("  ╭", "  ┌")):
        tag = "panel" if line.startswith("  ╭") else "table"
        current = [tag, i, width(line)]
        blocks.append(current)
    elif line.startswith(("  │", "  ├")) and current is not None:
        if width(line) != current[2]:
            current.append((i, width(line), line))
    elif line.startswith(("  ╰", "  └")):
        if current is not None and width(line) != current[2]:
            current.append((i, width(line), line))
        current = None

problems = []
for block in blocks:
    tag, start, expected = block[0], block[1], block[2]
    for item in block[3:]:
        problems.append(f"{tag} L{start} 起始宽 {expected}，L{item[0]} 宽 {item[1]} -> {item[2]}")

print(f"检测到 {len(blocks)} 个块（面板/表格）")
if problems:
    print(f"发现 {len(problems)} 处对齐问题：")
    for p in problems[:30]:
        print("  " + p)
else:
    print("对齐检查通过：每个块的边框与内容行宽一致")
