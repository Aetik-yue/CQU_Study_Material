# -*- coding: utf-8 -*-
"""汇总回归结果：每个脚本的 成功/错误/警告 计数"""
import glob
import os
import re
import sys

sys.stdout.reconfigure(encoding="utf-8")

files = sorted(glob.glob("test/reg_*.txt"))
print(f"{'脚本':<32}{'成功√':>6}{'错误×':>6}{'警告!':>6}")
print("-" * 52)
total_ok = 0
for f in files:
    text = open(f, encoding="utf-8", errors="replace").read()
    ok = len(re.findall("√", text))
    err = len(re.findall("×", text))
    warn = len(re.findall("! ", text))
    total_ok += ok
    print(f"{os.path.basename(f)[4:-4]:<32}{ok:>6}{err:>6}{warn:>6}")
print("-" * 52)
print(f"合计成功提示 {total_ok} 条；脚本 {len(files)} 个")
