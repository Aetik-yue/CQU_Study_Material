"""西瓜数据集 3.0（《机器学习》表 4.3）。

这个文件只负责保存和提供数据，不依赖 pandas，便于其它脚本直接加载。
由于文件名包含点号，普通的 ``import data4.3`` 不能使用；其它脚本可用
``runpy.run_path("data4.3.py")`` 读取这里定义的函数和变量。
"""

from __future__ import annotations

import csv
from pathlib import Path
from typing import Any

import numpy as np


COLUMNS = ["编号", "色泽", "根蒂", "敲声", "纹理", "脐部", "触感", "密度", "含糖率", "好瓜"]
ID_COLUMN = "编号"
LABEL_COLUMN = "好瓜"
DISCRETE_ATTRIBUTES = ["色泽", "根蒂", "敲声", "纹理", "脐部", "触感"]
CONTINUOUS_ATTRIBUTES = ["密度", "含糖率"]
FEATURE_COLUMNS = DISCRETE_ATTRIBUTES + CONTINUOUS_ATTRIBUTES

DATA = [
    [1, "青绿", "蜷缩", "浊响", "清晰", "凹陷", "硬滑", 0.697, 0.460, "是"],
    [2, "乌黑", "蜷缩", "沉闷", "清晰", "凹陷", "硬滑", 0.774, 0.376, "是"],
    [3, "乌黑", "蜷缩", "浊响", "清晰", "凹陷", "硬滑", 0.634, 0.264, "是"],
    [4, "青绿", "蜷缩", "沉闷", "清晰", "凹陷", "硬滑", 0.608, 0.318, "是"],
    [5, "浅白", "蜷缩", "浊响", "清晰", "凹陷", "硬滑", 0.556, 0.215, "是"],
    [6, "青绿", "稍蜷", "浊响", "清晰", "稍凹", "软粘", 0.403, 0.237, "是"],
    [7, "乌黑", "稍蜷", "浊响", "稍糊", "稍凹", "软粘", 0.481, 0.149, "是"],
    [8, "乌黑", "稍蜷", "浊响", "清晰", "稍凹", "硬滑", 0.437, 0.211, "是"],
    [9, "乌黑", "稍蜷", "沉闷", "稍糊", "稍凹", "硬滑", 0.666, 0.091, "否"],
    [10, "青绿", "硬挺", "清脆", "清晰", "平坦", "软粘", 0.243, 0.267, "否"],
    [11, "浅白", "硬挺", "清脆", "模糊", "平坦", "硬滑", 0.245, 0.057, "否"],
    [12, "浅白", "蜷缩", "浊响", "模糊", "平坦", "软粘", 0.343, 0.099, "否"],
    [13, "青绿", "稍蜷", "浊响", "稍糊", "凹陷", "硬滑", 0.639, 0.161, "否"],
    [14, "浅白", "稍蜷", "沉闷", "稍糊", "凹陷", "硬滑", 0.657, 0.198, "否"],
    [15, "乌黑", "稍蜷", "浊响", "清晰", "稍凹", "软粘", 0.360, 0.370, "否"],
    [16, "浅白", "蜷缩", "浊响", "模糊", "平坦", "硬滑", 0.593, 0.042, "否"],
    [17, "青绿", "蜷缩", "沉闷", "稍糊", "稍凹", "硬滑", 0.719, 0.103, "否"],
]


def get_dataset(drop_id: bool = True) -> list[dict[str, Any]]:
    """以字典列表形式返回数据集。

    Args:
        drop_id: 是否去掉“编号”列。训练模型通常不需要编号。
    """
    rows = []
    for values in DATA:
        row = dict(zip(COLUMNS, values))
        if drop_id:
            row.pop(ID_COLUMN)
        rows.append(row)
    return rows


def get_numeric_xy(label_positive: str = "是") -> tuple[np.ndarray, np.ndarray]:
    """返回适合逻辑回归的二维连续特征 X 和 0/1 标签 y。"""
    rows = get_dataset(drop_id=True)
    x = np.array([[row["密度"], row["含糖率"]] for row in rows], dtype=float)
    y = np.array([1 if row[LABEL_COLUMN] == label_positive else 0 for row in rows], dtype=int)
    return x, y


def save_csv(path: str | Path, drop_id: bool = False) -> Path:
    """把数据集保存为 UTF-8 CSV 文件。"""
    path = Path(path)
    columns = [column for column in COLUMNS if not (drop_id and column == ID_COLUMN)]
    rows = get_dataset(drop_id=drop_id)

    with path.open("w", newline="", encoding="utf-8-sig") as file:
        writer = csv.DictWriter(file, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)
    return path


def print_table(rows: list[dict[str, Any]] | None = None) -> None:
    """用简单的等宽文本打印数据，避免依赖第三方表格库。"""
    rows = rows or get_dataset(drop_id=False)
    columns = list(rows[0].keys())
    widths = {
        column: max(len(str(column)), *(len(str(row[column])) for row in rows))
        for column in columns
    }

    header = "  ".join(str(column).ljust(widths[column]) for column in columns)
    print(header)
    print("-" * len(header))
    for row in rows:
        print("  ".join(str(row[column]).ljust(widths[column]) for column in columns))


if __name__ == "__main__":
    print("西瓜数据集 3.0，共 17 条样本：")
    print_table()
