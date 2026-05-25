"""
使用手写对率回归算法对鸢尾花数据集进行训练和可视化
"""

import csv
from pathlib import Path
from typing import Iterable

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np

plt.rcParams["font.sans-serif"] = ["SimHei", "DejaVu Sans"]
plt.rcParams["axes.unicode_minus"] = False


def read_iris_data(csv_path: Path) -> tuple[np.ndarray, np.ndarray]:
    """读取鸢尾花数据集，选择setosa和versicolor两类，使用前两个特征"""
    encodings = ("utf-8-sig", "gb18030", "gbk", "utf-8")

    for encoding in encodings:
        try:
            with csv_path.open("r", encoding=encoding, newline="") as file:
                reader = csv.reader(file)
                header = next(reader)  # 跳过表头

                X_list = []
                y_list = []

                for row in reader:
                    if len(row) < 6:
                        continue

                    # 解析特征（花萼长度、花萼宽度）- CSV格式: "",Sepal.Length,Sepal.Width,...
                    try:
                        sepal_length = float(row[2].strip().strip('"'))
                        sepal_width = float(row[3].strip().strip('"'))
                    except (ValueError, IndexError):
                        continue

                    # 解析标签：只取setosa(0)和versicolor(1)
                    species = row[5].strip().strip('"')
                    if species == "setosa":
                        label = 0
                    elif species == "versicolor":
                        label = 1
                    else:  # virginica 跳过
                        continue

                    X_list.append([sepal_length, sepal_width])
                    y_list.append(label)

                return np.array(X_list, dtype=np.float64), np.array(y_list, dtype=np.float64)

        except UnicodeDecodeError:
            continue

    raise ValueError(f"无法读取文件: {csv_path}")


def standardize_features(X: np.ndarray) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    """Z-score标准化"""
    mean = np.mean(X, axis=0)
    std = np.std(X, axis=0)
    std[std == 0] = 1.0  # 防止除以零
    X_scaled = (X - mean) / std
    return X_scaled, mean, std


def train_test_split(
    X: np.ndarray, y: np.ndarray, test_size: float = 0.2, random_state: int = 42
) -> tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
    """划分训练集和测试集"""
    np.random.seed(random_state)
    n_samples = X.shape[0]
    indices = np.random.permutation(n_samples)

    test_count = int(n_samples * test_size)
    test_indices = indices[:test_count]
    train_indices = indices[test_count:]

    return (
        X[train_indices],
        X[test_indices],
        y[train_indices],
        y[test_indices],
    )


def stable_sigmoid(z: np.ndarray) -> np.ndarray:
    """数值稳定的Sigmoid函数"""
    probs = np.empty_like(z, dtype=np.float64)
    non_negative = z >= 0
    probs[non_negative] = 1.0 / (1.0 + np.exp(-z[non_negative]))
    exp_z = np.exp(z[~non_negative])
    probs[~non_negative] = exp_z / (1.0 + exp_z)
    return probs


def compute_loss(
    X_bias: np.ndarray, y: np.ndarray, weights: np.ndarray, reg: float = 0.0
) -> float:
    """计算交叉熵损失"""
    probs = stable_sigmoid(X_bias @ weights)
    probs = np.clip(probs, 1e-12, 1.0 - 1e-12)
    data_loss = -np.mean(y * np.log(probs) + (1.0 - y) * np.log(1.0 - probs))
    reg_loss = 0.5 * reg * np.sum(weights[1:] ** 2)
    return data_loss + reg_loss


def train_logistic_regression(
    X: np.ndarray,
    y: np.ndarray,
    learning_rate: float = 0.1,
    n_iterations: int = 1000,
    regularization: float = 0.01,
    verbose: bool = True,
) -> dict:
    """
    使用梯度下降训练对率回归模型
    """
    n_samples, n_features = X.shape

    # 添加偏置项
    X_bias = np.column_stack([np.ones(n_samples), X])

    # 初始化权重
    weights = np.zeros(n_features + 1, dtype=np.float64)

    loss_history = []
    accuracy_history = []

    for iteration in range(n_iterations):
        # 前向传播
        logits = X_bias @ weights
        probs = stable_sigmoid(logits)

        # 计算损失
        loss = compute_loss(X_bias, y, weights, regularization)
        loss_history.append(loss)

        # 计算准确率
        preds = (probs >= 0.5).astype(int)
        accuracy = np.mean(preds == y)
        accuracy_history.append(accuracy)

        # 计算梯度
        gradient = X_bias.T @ (probs - y) / n_samples
        gradient[1:] += regularization * weights[1:]

        # 更新权重
        weights -= learning_rate * gradient

        if verbose and iteration % 100 == 0:
            print(f"迭代 {iteration:4d}: 损失 = {loss:.6f}, 准确率 = {accuracy:.4f}")

    return {
        "weights": weights,
        "loss_history": np.array(loss_history),
        "accuracy_history": np.array(accuracy_history),
        "n_iterations": n_iterations,
    }


def predict(X: np.ndarray, weights: np.ndarray) -> tuple[np.ndarray, np.ndarray]:
    """预测概率和标签"""
    n_samples = X.shape[0]
    X_bias = np.column_stack([np.ones(n_samples), X])
    probs = stable_sigmoid(X_bias @ weights)
    labels = (probs >= 0.5).astype(int)
    return probs, labels


def evaluate(y_true: np.ndarray, y_pred: np.ndarray) -> dict:
    """评估模型性能"""
    accuracy = np.mean(y_true == y_pred)

    # 混淆矩阵
    tp = np.sum((y_true == 1) & (y_pred == 1))
    tn = np.sum((y_true == 0) & (y_pred == 0))
    fp = np.sum((y_true == 0) & (y_pred == 1))
    fn = np.sum((y_true == 1) & (y_pred == 0))

    precision = tp / (tp + fp) if (tp + fp) > 0 else 0.0
    recall = tp / (tp + fn) if (tp + fn) > 0 else 0.0
    f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0.0

    return {
        "accuracy": accuracy,
        "precision": precision,
        "recall": recall,
        "f1": f1,
        "confusion_matrix": np.array([[tn, fp], [fn, tp]]),
    }


def plot_feature_distribution(
    X: np.ndarray, y: np.ndarray, save_path: Path
) -> None:
    """绘制特征散点分布图"""
    fig, axes = plt.subplots(1, 2, figsize=(14, 5))

    # 图1：原始特征分布
    ax1 = axes[0]
    setosa_mask = y == 0
    versicolor_mask = y == 1

    ax1.scatter(
        X[setosa_mask, 0],
        X[setosa_mask, 1],
        c="#2E86AB",
        s=80,
        alpha=0.7,
        edgecolors="white",
        linewidth=1,
        label="Setosa (山鸢尾)",
    )
    ax1.scatter(
        X[versicolor_mask, 0],
        X[versicolor_mask, 1],
        c="#A23B72",
        s=80,
        alpha=0.7,
        edgecolors="white",
        linewidth=1,
        label="Versicolor (变色鸢尾)",
    )

    ax1.set_xlabel("Sepal Length (花萼长度)", fontsize=12)
    ax1.set_ylabel("Sepal Width (花萼宽度)", fontsize=12)
    ax1.set_title("Iris Feature Distribution\n(鸢尾花特征分布)", fontsize=14)
    ax1.legend(loc="best", fontsize=10)
    ax1.grid(True, alpha=0.3)

    # 图2：特征箱线图
    ax2 = axes[1]
    feature_names = ["Sepal Length", "Sepal Width"]
    setosa_data = [X[setosa_mask, 0], X[setosa_mask, 1]]
    versicolor_data = [X[versicolor_mask, 0], X[versicolor_mask, 1]]

    positions = [1, 2, 4, 5]
    bp1 = ax2.boxplot(
        setosa_data,
        positions=[1, 2],
        widths=0.6,
        patch_artist=True,
        boxprops=dict(facecolor="#2E86AB", alpha=0.7),
        medianprops=dict(color="white", linewidth=2),
    )
    bp2 = ax2.boxplot(
        versicolor_data,
        positions=[4, 5],
        widths=0.6,
        patch_artist=True,
        boxprops=dict(facecolor="#A23B72", alpha=0.7),
        medianprops=dict(color="white", linewidth=2),
    )

    ax2.set_xticks([1.5, 4.5])
    ax2.set_xticklabels(feature_names, fontsize=11)
    ax2.set_ylabel("Value", fontsize=12)
    ax2.set_title("Feature Distribution by Class\n(各类别特征分布)", fontsize=14)
    ax2.legend(
        [bp1["boxes"][0], bp2["boxes"][0]],
        ["Setosa", "Versicolor"],
        loc="best",
        fontsize=10,
    )
    ax2.grid(True, alpha=0.3, axis="y")

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"特征分布图已保存: {save_path}")


def plot_training_curves(
    loss_history: np.ndarray,
    accuracy_history: np.ndarray,
    save_path: Path,
) -> None:
    """绘制训练过程曲线"""
    fig, axes = plt.subplots(1, 2, figsize=(14, 5))

    # 损失曲线
    ax1 = axes[0]
    ax1.plot(loss_history, "b-", linewidth=2, alpha=0.8)
    ax1.fill_between(range(len(loss_history)), loss_history, alpha=0.3)
    ax1.set_xlabel("Iteration (迭代次数)", fontsize=12)
    ax1.set_ylabel("Loss (损失值)", fontsize=12)
    ax1.set_title("Training Loss Curve\n(训练损失曲线)", fontsize=14)
    ax1.grid(True, alpha=0.3)

    # 标注关键点
    min_loss_idx = np.argmin(loss_history)
    ax1.scatter(
        [min_loss_idx],
        [loss_history[min_loss_idx]],
        color="red",
        s=100,
        zorder=5,
        label=f"Min Loss: {loss_history[min_loss_idx]:.4f}",
    )
    ax1.legend(fontsize=10)

    # 准确率曲线
    ax2 = axes[1]
    ax2.plot(accuracy_history, "g-", linewidth=2, alpha=0.8)
    ax2.fill_between(range(len(accuracy_history)), accuracy_history, alpha=0.3)
    ax2.set_xlabel("Iteration (迭代次数)", fontsize=12)
    ax2.set_ylabel("Accuracy (准确率)", fontsize=12)
    ax2.set_title("Training Accuracy Curve\n(训练准确率曲线)", fontsize=14)
    ax2.set_ylim([0, 1.05])
    ax2.grid(True, alpha=0.3)

    # 标注关键点
    max_acc_idx = np.argmax(accuracy_history)
    ax2.scatter(
        [max_acc_idx],
        [accuracy_history[max_acc_idx]],
        color="red",
        s=100,
        zorder=5,
        label=f"Max Acc: {accuracy_history[max_acc_idx]:.4f}",
    )
    ax2.legend(fontsize=10)

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"训练曲线图已保存: {save_path}")


def plot_decision_boundary(
    X: np.ndarray,
    y: np.ndarray,
    weights: np.ndarray,
    save_path: Path,
    X_test: np.ndarray = None,
    y_test: np.ndarray = None,
) -> None:
    """绘制决策边界可视化图"""
    fig, axes = plt.subplots(1, 2, figsize=(16, 6))

    # 创建网格
    x_min, x_max = X[:, 0].min() - 0.5, X[:, 0].max() + 0.5
    y_min, y_max = X[:, 1].min() - 0.5, X[:, 1].max() + 0.5

    xx, yy = np.meshgrid(
        np.linspace(x_min, x_max, 200), np.linspace(y_min, y_max, 200)
    )

    # 预测网格点的概率
    grid_points = np.c_[xx.ravel(), yy.ravel()]
    Z, _ = predict(grid_points, weights)
    Z = Z.reshape(xx.shape)

    # 图1：概率热力图
    ax1 = axes[0]
    contour = ax1.contourf(xx, yy, Z, levels=20, cmap="RdYlBu", alpha=0.8)
    ax1.contour(xx, yy, Z, levels=[0.5], colors="black", linewidths=2, linestyles="--")

    # 绘制数据点
    setosa_mask = y == 0
    versicolor_mask = y == 1

    ax1.scatter(
        X[setosa_mask, 0],
        X[setosa_mask, 1],
        c="#2E86AB",
        s=100,
        edgecolors="white",
        linewidth=2,
        label="Setosa (山鸢尾)",
        zorder=5,
    )
    ax1.scatter(
        X[versicolor_mask, 0],
        X[versicolor_mask, 1],
        c="#A23B72",
        s=100,
        edgecolors="white",
        linewidth=2,
        label="Versicolor (变色鸢尾)",
        zorder=5,
    )

    cbar = plt.colorbar(contour, ax=ax1)
    cbar.set_label("P(Versicolor)", fontsize=11)

    ax1.set_xlabel("Sepal Length (标准化)", fontsize=12)
    ax1.set_ylabel("Sepal Width (标准化)", fontsize=12)
    ax1.set_title("Decision Boundary (Probability Heatmap)\n(决策边界 - 概率热力图)", fontsize=14)
    ax1.legend(loc="best", fontsize=10)
    ax1.grid(True, alpha=0.3)

    # 图2：分类结果可视化
    ax2 = axes[1]

    # 绘制决策区域
    Z_labels = (Z >= 0.5).astype(int)
    ax2.contourf(xx, yy, Z_labels, levels=[-0.5, 0.5, 1.5], colors=["#2E86AB", "#A23B72"], alpha=0.3)
    ax2.contour(xx, yy, Z, levels=[0.5], colors="black", linewidths=2)

    # 绘制训练集
    ax2.scatter(
        X[setosa_mask, 0],
        X[setosa_mask, 1],
        c="#2E86AB",
        s=100,
        marker="o",
        edgecolors="white",
        linewidth=2,
        label="Setosa (Train)",
        zorder=5,
    )
    ax2.scatter(
        X[versicolor_mask, 0],
        X[versicolor_mask, 1],
        c="#A23B72",
        s=100,
        marker="o",
        edgecolors="white",
        linewidth=2,
        label="Versicolor (Train)",
        zorder=5,
    )

    # 绘制测试集
    if X_test is not None and y_test is not None:
        test_setosa = y_test == 0
        test_versicolor = y_test == 1

        ax2.scatter(
            X_test[test_setosa, 0],
            X_test[test_setosa, 1],
            c="#2E86AB",
            s=150,
            marker="*",
            edgecolors="black",
            linewidth=1.5,
            label="Setosa (Test)",
            zorder=6,
        )
        ax2.scatter(
            X_test[test_versicolor, 0],
            X_test[test_versicolor, 1],
            c="#A23B72",
            s=150,
            marker="*",
            edgecolors="black",
            linewidth=1.5,
            label="Versicolor (Test)",
            zorder=6,
        )

    ax2.set_xlabel("Sepal Length (标准化)", fontsize=12)
    ax2.set_ylabel("Sepal Width (标准化)", fontsize=12)
    ax2.set_title("Classification Result\n(分类结果可视化)", fontsize=14)
    ax2.legend(loc="best", fontsize=10)
    ax2.grid(True, alpha=0.3)

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"决策边界图已保存: {save_path}")


def plot_confusion_matrix(cm: np.ndarray, save_path: Path) -> None:
    """绘制混淆矩阵"""
    fig, ax = plt.subplots(figsize=(8, 6))

    im = ax.imshow(cm, interpolation="nearest", cmap="Blues")
    ax.figure.colorbar(im, ax=ax)

    classes = ["Setosa", "Versicolor"]
    ax.set(
        xticks=np.arange(cm.shape[1]),
        yticks=np.arange(cm.shape[0]),
        xticklabels=classes,
        yticklabels=classes,
        ylabel="True Label (真实标签)",
        xlabel="Predicted Label (预测标签)",
        title="Confusion Matrix\n(混淆矩阵)",
    )

    # 在每个格子中显示数值
    thresh = cm.max() / 2.0
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            ax.text(
                j,
                i,
                format(cm[i, j], "d"),
                ha="center",
                va="center",
                color="white" if cm[i, j] > thresh else "black",
                fontsize=20,
                fontweight="bold",
            )

    plt.tight_layout()
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"混淆矩阵图已保存: {save_path}")


def main():
    # 路径设置
    data_path = Path(r"C:\Users\yanha\Desktop\机器学习\实验\数据集\数据集\iris.csv")
    output_dir = Path(__file__).resolve().parent / "iris_outputs"
    output_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 60)
    print("鸢尾花数据集对率回归训练")
    print("=" * 60)

    # 1. 数据加载
    print("\n[1] 加载数据...")
    X, y = read_iris_data(data_path)
    print(f"    数据形状: X={X.shape}, y={y.shape}")
    print(f"    类别分布: Setosa={np.sum(y==0)}, Versicolor={np.sum(y==1)}")

    # 2. 数据划分
    print("\n[2] 划分训练集和测试集...")
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    print(f"    训练集: {X_train.shape[0]} 样本")
    print(f"    测试集: {X_test.shape[0]} 样本")

    # 3. 特征标准化
    print("\n[3] 特征标准化...")
    X_train_scaled, mean, std = standardize_features(X_train)
    X_test_scaled = (X_test - mean) / std
    print(f"    训练集均值: {mean}")
    print(f"    训练集标准差: {std}")

    # 4. 绘制特征分布图
    print("\n[4] 生成特征分布图...")
    plot_feature_distribution(X_train_scaled, y_train, output_dir / "01_feature_distribution.png")

    # 5. 模型训练
    print("\n[5] 训练模型...")
    print("-" * 40)
    model = train_logistic_regression(
        X_train_scaled,
        y_train,
        learning_rate=0.5,
        n_iterations=500,
        regularization=0.01,
        verbose=True,
    )
    print("-" * 40)

    # 6. 绘制训练曲线
    print("\n[6] 生成训练曲线图...")
    plot_training_curves(
        model["loss_history"],
        model["accuracy_history"],
        output_dir / "02_training_curves.png",
    )

    # 7. 模型评估
    print("\n[7] 模型评估...")

    # 训练集评估
    train_probs, train_preds = predict(X_train_scaled, model["weights"])
    train_metrics = evaluate(y_train, train_preds)

    # 测试集评估
    test_probs, test_preds = predict(X_test_scaled, model["weights"])
    test_metrics = evaluate(y_test, test_preds)

    print(f"\n    训练集准确率: {train_metrics['accuracy']:.4f}")
    print(f"    测试集准确率: {test_metrics['accuracy']:.4f}")
    print(f"\n    精确率: {test_metrics['precision']:.4f}")
    print(f"    召回率: {test_metrics['recall']:.4f}")
    print(f"    F1分数: {test_metrics['f1']:.4f}")

    # 8. 绘制决策边界
    print("\n[8] 生成决策边界图...")
    plot_decision_boundary(
        X_train_scaled,
        y_train,
        model["weights"],
        output_dir / "03_decision_boundary.png",
        X_test_scaled,
        y_test,
    )

    # 9. 绘制混淆矩阵
    print("\n[9] 生成混淆矩阵图...")
    plot_confusion_matrix(test_metrics["confusion_matrix"], output_dir / "04_confusion_matrix.png")

    # 10. 输出模型参数
    print("\n[10] 模型参数:")
    print(f"    偏置项 (b): {model['weights'][0]:.6f}")
    print(f"    权重 w1: {model['weights'][1]:.6f}")
    print(f"    权重 w2: {model['weights'][2]:.6f}")
    print(f"    决策边界方程: {model['weights'][1]:.4f}*x1 + {model['weights'][2]:.4f}*x2 + {model['weights'][0]:.4f} = 0")

    print("\n" + "=" * 60)
    print(f"训练完成！所有结果已保存到: {output_dir}")
    print("=" * 60)


if __name__ == "__main__":
    main()
