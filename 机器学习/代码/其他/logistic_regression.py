"""从零实现二分类逻辑回归。

默认演示使用同目录下 data4.3.py 的西瓜数据集连续属性（密度、含糖率），
也可以通过 ``--dataset synthetic`` 使用模拟数据。
"""

from __future__ import annotations

import argparse
import runpy
from pathlib import Path

import matplotlib
import numpy as np

matplotlib.use("Agg")
import matplotlib.pyplot as plt


plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "Arial Unicode MS", "DejaVu Sans"]
plt.rcParams["axes.unicode_minus"] = False


class LogisticRegression:
    """使用批量梯度下降训练的二分类逻辑回归。"""

    def __init__(
        self,
        learning_rate: float = 0.1,
        n_iterations: int = 1000,
        tolerance: float = 1e-8,
        l2: float = 0.0,
        verbose: bool = False,
    ) -> None:
        if learning_rate <= 0:
            raise ValueError("learning_rate 必须大于 0")
        if n_iterations <= 0:
            raise ValueError("n_iterations 必须大于 0")
        if tolerance < 0:
            raise ValueError("tolerance 不能为负数")
        if l2 < 0:
            raise ValueError("l2 不能为负数")

        self.learning_rate = learning_rate
        self.n_iterations = n_iterations
        self.tolerance = tolerance
        self.l2 = l2
        self.verbose = verbose
        self.weights: np.ndarray | None = None
        self.bias: float = 0.0
        self.loss_history: list[float] = []

    @staticmethod
    def sigmoid(z: np.ndarray) -> np.ndarray:
        """数值稳定的 sigmoid 函数。"""
        z = np.clip(z, -500, 500)
        return 1.0 / (1.0 + np.exp(-z))

    def _check_is_fitted(self) -> None:
        if self.weights is None:
            raise RuntimeError("模型还没有训练，请先调用 fit(X, y)")

    def compute_loss(self, y_true: np.ndarray, y_pred: np.ndarray) -> float:
        """计算带可选 L2 正则项的交叉熵损失。"""
        eps = 1e-15
        y_pred = np.clip(y_pred, eps, 1.0 - eps)
        data_loss = -np.mean(y_true * np.log(y_pred) + (1.0 - y_true) * np.log(1.0 - y_pred))
        if self.weights is None or self.l2 == 0:
            return float(data_loss)
        reg_loss = self.l2 * np.sum(self.weights**2) / (2 * len(y_true))
        return float(data_loss + reg_loss)

    def fit(self, x: np.ndarray, y: np.ndarray) -> "LogisticRegression":
        """训练模型并返回 self。"""
        x = np.asarray(x, dtype=float)
        y = np.asarray(y, dtype=float).reshape(-1)

        if x.ndim != 2:
            raise ValueError("X 必须是二维数组，形状为 (n_samples, n_features)")
        if len(x) != len(y):
            raise ValueError("X 和 y 的样本数量必须一致")
        if not set(np.unique(y)).issubset({0.0, 1.0}):
            raise ValueError("y 必须只包含 0 和 1")

        n_samples, n_features = x.shape
        self.weights = np.zeros(n_features, dtype=float)
        self.bias = 0.0
        self.loss_history = []
        previous_loss = float("inf")

        for i in range(self.n_iterations):
            y_pred = self.sigmoid(x @ self.weights + self.bias)
            error = y_pred - y

            dw = (x.T @ error + self.l2 * self.weights) / n_samples
            db = float(np.mean(error))
            self.weights -= self.learning_rate * dw
            self.bias -= self.learning_rate * db

            current_pred = self.sigmoid(x @ self.weights + self.bias)
            loss = self.compute_loss(y, current_pred)
            self.loss_history.append(loss)

            if self.verbose and (i == 0 or (i + 1) % 100 == 0):
                print(f"迭代 {i + 1:4d}: 损失 = {loss:.6f}")
            if abs(previous_loss - loss) < self.tolerance:
                if self.verbose:
                    print(f"损失变化小于 tolerance={self.tolerance:g}，提前停止。")
                break
            previous_loss = loss

        return self

    def predict_proba(self, x: np.ndarray) -> np.ndarray:
        """预测正类概率 P(y=1|X)。"""
        self._check_is_fitted()
        x = np.asarray(x, dtype=float)
        return self.sigmoid(x @ self.weights + self.bias)

    def predict(self, x: np.ndarray, threshold: float = 0.5) -> np.ndarray:
        """预测类别标签。"""
        if not 0 <= threshold <= 1:
            raise ValueError("threshold 必须在 [0, 1] 范围内")
        return (self.predict_proba(x) >= threshold).astype(int)

    def score(self, x: np.ndarray, y: np.ndarray) -> float:
        """计算准确率。"""
        y = np.asarray(y).reshape(-1)
        return float(np.mean(self.predict(x) == y))


def standardize(x: np.ndarray) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    """Z-score 标准化，返回标准化后的 X、均值和标准差。"""
    x = np.asarray(x, dtype=float)
    mean = np.mean(x, axis=0)
    std = np.std(x, axis=0)
    std[std == 0] = 1.0
    return (x - mean) / std, mean, std


def apply_standardize(x: np.ndarray, mean: np.ndarray, std: np.ndarray) -> np.ndarray:
    return (np.asarray(x, dtype=float) - mean) / std


def train_test_split(
    x: np.ndarray,
    y: np.ndarray,
    test_size: float = 0.3,
    random_state: int | None = None,
    stratify: bool = False,
) -> tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    """简单训练集/测试集划分。

    stratify=True 时按类别分别抽样，适合西瓜数据集这类小样本二分类数据。
    """
    if not 0 < test_size < 1:
        raise ValueError("test_size 必须在 (0, 1) 范围内")

    x = np.asarray(x)
    y = np.asarray(y)
    rng = np.random.default_rng(random_state)
    n_samples = len(x)

    if stratify:
        train_parts = []
        test_parts = []
        for cls in np.unique(y):
            cls_indices = np.where(y == cls)[0]
            cls_indices = rng.permutation(cls_indices)
            test_count = max(1, min(len(cls_indices) - 1, int(round(len(cls_indices) * test_size))))
            test_parts.append(cls_indices[:test_count])
            train_parts.append(cls_indices[test_count:])
        train_indices = rng.permutation(np.concatenate(train_parts))
        test_indices = rng.permutation(np.concatenate(test_parts))
    else:
        indices = rng.permutation(n_samples)
        test_count = max(1, min(n_samples - 1, int(round(n_samples * test_size))))
        test_indices = indices[:test_count]
        train_indices = indices[test_count:]
    return x[train_indices], x[test_indices], y[train_indices], y[test_indices]


def generate_data(n_samples: int = 500, random_state: int = 42) -> tuple[np.ndarray, np.ndarray]:
    """生成二维二分类模拟数据。"""
    if n_samples < 2:
        raise ValueError("n_samples 至少为 2")

    rng = np.random.default_rng(random_state)
    n_class0 = n_samples // 2
    n_class1 = n_samples - n_class0
    x_class0 = rng.normal(size=(n_class0, 2)) + np.array([-2.0, -2.0])
    x_class1 = rng.normal(size=(n_class1, 2)) + np.array([2.0, 2.0])
    x = np.vstack([x_class0, x_class1])
    y = np.hstack([np.zeros(n_class0, dtype=int), np.ones(n_class1, dtype=int)])
    indices = rng.permutation(n_samples)
    return x[indices], y[indices]


def load_watermelon_data() -> tuple[np.ndarray, np.ndarray]:
    """从 data4.3.py 读取西瓜数据集中的连续属性。"""
    data_file = Path(__file__).with_name("data4.3.py")
    namespace = runpy.run_path(str(data_file))
    return namespace["get_numeric_xy"]()


def confusion_matrix(y_true: np.ndarray, y_pred: np.ndarray) -> np.ndarray:
    """返回 [[TN, FP], [FN, TP]] 格式的混淆矩阵。"""
    y_true = np.asarray(y_true).reshape(-1)
    y_pred = np.asarray(y_pred).reshape(-1)
    tp = np.sum((y_true == 1) & (y_pred == 1))
    tn = np.sum((y_true == 0) & (y_pred == 0))
    fp = np.sum((y_true == 0) & (y_pred == 1))
    fn = np.sum((y_true == 1) & (y_pred == 0))
    return np.array([[tn, fp], [fn, tp]], dtype=int)


def classification_metrics(y_true: np.ndarray, y_pred: np.ndarray) -> dict[str, float | np.ndarray]:
    """计算常见二分类指标。"""
    cm = confusion_matrix(y_true, y_pred)
    tn, fp, fn, tp = cm.ravel()
    total = tp + tn + fp + fn
    accuracy = (tp + tn) / total if total else 0.0
    precision = tp / (tp + fp) if (tp + fp) else 0.0
    recall = tp / (tp + fn) if (tp + fn) else 0.0
    f1 = 2 * precision * recall / (precision + recall) if (precision + recall) else 0.0
    return {
        "accuracy": float(accuracy),
        "precision": float(precision),
        "recall": float(recall),
        "f1_score": float(f1),
        "confusion_matrix": cm,
    }


def plot_results(
    model: LogisticRegression,
    x: np.ndarray,
    y: np.ndarray,
    feature_names: tuple[str, str],
    output_path: str | Path = "logistic_regression_result.png",
) -> Path:
    """绘制决策边界和损失曲线。"""
    output_path = Path(output_path)
    fig, axes = plt.subplots(1, 2, figsize=(12, 5))

    ax1 = axes[0]
    x_min, x_max = x[:, 0].min() - 0.8, x[:, 0].max() + 0.8
    y_min, y_max = x[:, 1].min() - 0.8, x[:, 1].max() + 0.8
    xx, yy = np.meshgrid(np.linspace(x_min, x_max, 200), np.linspace(y_min, y_max, 200))
    grid = np.c_[xx.ravel(), yy.ravel()]
    z = model.predict_proba(grid).reshape(xx.shape)

    ax1.contourf(xx, yy, z, levels=20, alpha=0.75, cmap="RdYlBu_r")
    ax1.contour(xx, yy, z, levels=[0.5], colors="black", linewidths=2)
    ax1.scatter(x[y == 0, 0], x[y == 0, 1], c="#2f6fdd", label="负类/否", edgecolors="white")
    ax1.scatter(x[y == 1, 0], x[y == 1, 1], c="#d64045", label="正类/是", edgecolors="white")
    ax1.set_xlabel(feature_names[0])
    ax1.set_ylabel(feature_names[1])
    ax1.set_title("决策边界")
    ax1.legend()

    ax2 = axes[1]
    ax2.plot(model.loss_history, color="#2f6fdd", linewidth=1.5)
    ax2.set_xlabel("迭代次数")
    ax2.set_ylabel("损失")
    ax2.set_title("损失收敛曲线")
    ax2.grid(True, alpha=0.3)

    fig.tight_layout()
    fig.savefig(output_path, dpi=150, bbox_inches="tight")
    plt.close(fig)
    return output_path


def run_demo(dataset: str, verbose: bool) -> None:
    if dataset == "synthetic":
        x, y = generate_data(n_samples=500, random_state=42)
        feature_names = ("特征1（标准化）", "特征2（标准化）")
        test_size = 0.2
    else:
        x, y = load_watermelon_data()
        feature_names = ("密度（标准化）", "含糖率（标准化）")
        test_size = 0.3

    print("=" * 60)
    print("对数几率回归（逻辑回归）算法实现")
    print("=" * 60)
    print(f"\n[1] 数据集: {dataset}")
    print(f"    数据形状: X={x.shape}, y={y.shape}")
    print(f"    类别分布: 负类={np.sum(y == 0)}, 正类={np.sum(y == 1)}")

    print("\n[2] 标准化并划分训练集/测试集...")
    x_scaled, _, _ = standardize(x)
    x_train, x_test, y_train, y_test = train_test_split(
        x_scaled, y, test_size=test_size, random_state=5, stratify=(dataset == "watermelon")
    )
    print(f"    训练集: {len(x_train)} 样本")
    print(f"    测试集: {len(x_test)} 样本")

    print("\n[3] 训练模型...")
    model = LogisticRegression(
        learning_rate=0.1,
        n_iterations=3000 if dataset == "watermelon" else 1000,
        tolerance=1e-10,
        l2=0.01,
        verbose=verbose,
    )
    model.fit(x_train, y_train)

    print("\n[4] 模型参数:")
    print(f"    权重: {np.round(model.weights, 4)}")
    print(f"    偏置: {model.bias:.4f}")

    print("\n[5] 模型评估:")
    print(f"    训练集准确率: {model.score(x_train, y_train):.4f}")
    print(f"    测试集准确率: {model.score(x_test, y_test):.4f}")

    y_pred = model.predict(x_test)
    metrics = classification_metrics(y_test, y_pred)
    cm = metrics["confusion_matrix"]
    print("\n[6] 详细分类指标:")
    print(f"    准确率 Accuracy : {metrics['accuracy']:.4f}")
    print(f"    精确率 Precision: {metrics['precision']:.4f}")
    print(f"    召回率 Recall   : {metrics['recall']:.4f}")
    print(f"    F1 分数         : {metrics['f1_score']:.4f}")
    print(f"    混淆矩阵 [[TN, FP], [FN, TP]]: {cm.tolist()}")

    print("\n[7] 保存可视化结果...")
    output_path = plot_results(model, x_scaled, y, feature_names)
    print(f"    已保存: {output_path}")
    print("\n算法实现完成！")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="从零实现逻辑回归二分类")
    parser.add_argument(
        "--dataset",
        choices=("watermelon", "synthetic"),
        default="watermelon",
        help="选择演示数据集，默认使用西瓜数据集",
    )
    parser.add_argument("--verbose", action="store_true", help="打印训练过程中的损失")
    return parser.parse_args()


if __name__ == "__main__":
    args = parse_args()
    run_demo(dataset=args.dataset, verbose=args.verbose)
