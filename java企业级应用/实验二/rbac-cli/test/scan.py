# -*- coding: utf-8 -*-
"""扫描测试输出：提取关键结果行（自动去除 ANSI 颜色码）"""
import re
import sys

sys.stdout.reconfigure(encoding="utf-8")
path = sys.argv[1]
keywords = sys.argv[2:] if len(sys.argv) > 2 else ["√", "×", "! ", "成功", "错误", "提示", "共 "]

ansi = re.compile(r"\x1b\[[0-9;?]*[a-zA-Z]")
with open(path, encoding="utf-8", errors="replace") as f:
    for i, line in enumerate(f, 1):
        plain = ansi.sub("", line)
        if any(k in plain for k in keywords):
            print(f"{i}: {plain.rstrip()}")
