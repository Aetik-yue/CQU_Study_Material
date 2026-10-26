# -*- coding: utf-8 -*-
"""把指定 bat 文件规范化为 CRLF + ASCII（无 BOM）"""
import sys

sys.stdout.reconfigure(encoding="utf-8")
for path in sys.argv[1:]:
    with open(path, "rb") as f:
        data = f.read()
    # 去 BOM
    if data.startswith(b"\xef\xbb\xbf"):
        data = data[3:]
    # 统一为 LF 后再转 CRLF
    data = data.replace(b"\r\n", b"\n").replace(b"\n", b"\r\n")
    with open(path, "wb") as f:
        f.write(data)
    # ASCII 检查
    bad = [b for b in data if b > 127]
    print(f"{path}: CRLF ok, bytes={len(data)}, non-ascii={len(bad)}")
