"""基于信息增益的决策树（ID3）示例。

默认使用同目录下的 data4.3.py 中的西瓜数据集 3.0。
支持离散属性和连续属性；连续属性会自动寻找最佳二分阈值。
"""

from __future__ import annotations

import math
import runpy
from collections import Counter
from pathlib import Path
from typing import Any


LABEL = "好瓜"
CONTINUOUS_ATTRIBUTES = {"密度", "含糖率"}


def load_watermelon_data() -> list[dict[str, Any]]:
    data_file = Path(__file__).with_name("data4.3.py")
    namespace = runpy.run_path(str(data_file))
    return namespace["get_dataset"](drop_id=True)


def entropy(rows: list[dict[str, Any]], label: str = LABEL) -> float:
    """计算信息熵。"""
    total = len(rows)
    counts = Counter(row[label] for row in rows)
    return -sum((count / total) * math.log2(count / total) for count in counts.values())


def majority_label(rows: list[dict[str, Any]], label: str = LABEL) -> str:
    """返回当前样本中的多数类别。"""
    return Counter(row[label] for row in rows).most_common(1)[0][0]


def information_gain_discrete(rows: list[dict[str, Any]], attr: str, label: str = LABEL) -> float:
    """离散属性的信息增益。"""
    total_entropy = entropy(rows, label)
    total = len(rows)
    values = sorted({row[attr] for row in rows})
    conditional_entropy = 0.0

    for value in values:
        subset = [row for row in rows if row[attr] == value]
        conditional_entropy += len(subset) / total * entropy(subset, label)
    return total_entropy - conditional_entropy


def candidate_thresholds(rows: list[dict[str, Any]], attr: str) -> list[float]:
    values = sorted({float(row[attr]) for row in rows})
    return [(left + right) / 2 for left, right in zip(values, values[1:])]


def information_gain_continuous(
    rows: list[dict[str, Any]], attr: str, label: str = LABEL
) -> tuple[float, float | None]:
    """连续属性的信息增益，返回 (最大增益, 最佳阈值)。"""
    total_entropy = entropy(rows, label)
    total = len(rows)
    best_gain = -1.0
    best_threshold = None

    for threshold in candidate_thresholds(rows, attr):
        left = [row for row in rows if float(row[attr]) <= threshold]
        right = [row for row in rows if float(row[attr]) > threshold]
        if not left or not right:
            continue
        conditional_entropy = (
            len(left) / total * entropy(left, label)
            + len(right) / total * entropy(right, label)
        )
        gain = total_entropy - conditional_entropy
        if gain > best_gain:
            best_gain = gain
            best_threshold = threshold
    return best_gain, best_threshold


def choose_best_attribute(
    rows: list[dict[str, Any]], attributes: list[str], label: str = LABEL
) -> tuple[str | None, float | None]:
    """选择当前节点的信息增益最大属性。"""
    best_attr = None
    best_threshold = None
    best_gain = -1.0

    for attr in attributes:
        if attr in CONTINUOUS_ATTRIBUTES:
            gain, threshold = information_gain_continuous(rows, attr, label)
        else:
            gain, threshold = information_gain_discrete(rows, attr, label), None

        if gain > best_gain:
            best_attr = attr
            best_threshold = threshold
            best_gain = gain
    return best_attr, best_threshold


def build_tree(
    rows: list[dict[str, Any]], attributes: list[str], label: str = LABEL
) -> dict[str, Any] | str:
    """递归构建决策树。"""
    labels = {row[label] for row in rows}
    if len(labels) == 1:
        return next(iter(labels))
    if not attributes:
        return majority_label(rows, label)

    best_attr, threshold = choose_best_attribute(rows, attributes, label)
    if best_attr is None:
        return majority_label(rows, label)

    node = {
        "attribute": best_attr,
        "threshold": threshold,
        "default": majority_label(rows, label),
        "branches": {},
    }

    remaining_attrs = attributes if threshold is not None else [a for a in attributes if a != best_attr]
    if threshold is None:
        for value in sorted({row[best_attr] for row in rows}):
            subset = [row for row in rows if row[best_attr] == value]
            node["branches"][value] = build_tree(subset, remaining_attrs, label)
    else:
        left = [row for row in rows if float(row[best_attr]) <= threshold]
        right = [row for row in rows if float(row[best_attr]) > threshold]
        node["branches"][f"<= {threshold:.3f}"] = build_tree(left, remaining_attrs, label)
        node["branches"][f"> {threshold:.3f}"] = build_tree(right, remaining_attrs, label)
    return node


def predict(tree: dict[str, Any] | str, sample: dict[str, Any]) -> str:
    """使用决策树预测单个样本。"""
    if not isinstance(tree, dict):
        return tree

    attr = tree["attribute"]
    threshold = tree["threshold"]
    if threshold is None:
        branch_key = sample.get(attr)
    else:
        branch_key = f"<= {threshold:.3f}" if float(sample[attr]) <= threshold else f"> {threshold:.3f}"

    subtree = tree["branches"].get(branch_key)
    if subtree is None:
        return tree["default"]
    return predict(subtree, sample)


def print_tree(tree: dict[str, Any] | str, indent: str = "") -> None:
    """以文本形式打印树结构。"""
    if not isinstance(tree, dict):
        print(f"{indent}-> 预测类别：{tree}")
        return

    attr = tree["attribute"]
    threshold = tree["threshold"]
    title = f"{attr} <= {threshold:.3f} ?" if threshold is not None else attr
    print(f"{indent}{title}")
    for value, subtree in tree["branches"].items():
        print(f"{indent}  [{value}]")
        print_tree(subtree, indent + "    ")


def accuracy(tree: dict[str, Any] | str, rows: list[dict[str, Any]], label: str = LABEL) -> float:
    correct = sum(predict(tree, row) == row[label] for row in rows)
    return correct / len(rows)


if __name__ == "__main__":
    dataset = load_watermelon_data()
    attrs = [key for key in dataset[0] if key != LABEL]
    tree = build_tree(dataset, attrs)

    print("\n决策树（文本形式）：")
    print_tree(tree)
    print(f"\n训练集准确率：{accuracy(tree, dataset):.4f}")
