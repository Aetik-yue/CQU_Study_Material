import argparse
import csv
from pathlib import Path
from typing import Iterable

import matplotlib

matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np

plt.rcParams["font.sans-serif"] = ["Microsoft YaHei", "SimHei", "DejaVu Sans"]
plt.rcParams["axes.unicode_minus"] = False


DEFAULT_DATA_DIR = Path(r"C:\Users\yanha\Desktop\机器学习\实验\数据集\数据集")
DEFAULT_TRAIN_PATH = DEFAULT_DATA_DIR / "3.0a.csv"
DEFAULT_PREDICT_PATH = DEFAULT_DATA_DIR / "4.0.csv"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="手写 Newton 法对率回归，不调用现成模型库。")
    parser.add_argument("--train-path", type=Path, default=DEFAULT_TRAIN_PATH, help="训练集 CSV 路径")
    parser.add_argument("--predict-path", type=Path, default=DEFAULT_PREDICT_PATH, help="待预测集 CSV 路径")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).resolve().parent / "outputs",
        help="结果输出目录",
    )
    parser.add_argument("--max-iter", type=int, default=50, help="Newton 最大迭代次数")
    parser.add_argument("--tol", type=float, default=1e-8, help="停止阈值")
    parser.add_argument("--regularization", type=float, default=1e-6, help="L2 正则化强度")
    parser.add_argument(
        "--disable-line-search",
        action="store_true",
        help="关闭回溯线搜索",
    )
    return parser.parse_args()


def read_csv_rows(csv_path: Path) -> list[list[str]]:
    encodings = ("utf-8-sig", "gb18030", "gbk", "utf-8")
    last_error = None

    for encoding in encodings:
        try:
            with csv_path.open("r", encoding=encoding, newline="") as file:
                return [row for row in csv.reader(file) if row]
        except UnicodeDecodeError as exc:
            last_error = exc

    raise UnicodeDecodeError(
        "unknown",
        b"",
        0,
        1,
        f"无法解码文件 {csv_path}，最后一次错误: {last_error}",
    )


def numeric_rows(rows: Iterable[list[str]]) -> list[list[float]]:
    parsed_rows: list[list[float]] = []
    for row in rows:
        values: list[float] = []
        try:
            for cell in row:
                text = cell.strip().strip('"')
                if text == "":
                    raise ValueError("empty cell")
                values.append(float(text))
        except ValueError:
            continue
        parsed_rows.append(values)
    return parsed_rows


def load_training_data(csv_path: Path) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    rows = numeric_rows(read_csv_rows(csv_path))
    if not rows:
        raise ValueError(f"训练集为空: {csv_path}")

    data = np.asarray(rows, dtype=np.float64)
    if data.shape[1] < 4:
        raise ValueError("训练集至少需要 4 列: 编号, 密度, 含糖率, 标签")

    sample_ids = data[:, 0].astype(int)
    features = data[:, 1:3]
    labels = data[:, 3]
    return sample_ids, features, labels


def load_predict_data(csv_path: Path) -> np.ndarray:
    rows = numeric_rows(read_csv_rows(csv_path))
    if not rows:
        raise ValueError(f"待预测集为空: {csv_path}")

    data = np.asarray(rows, dtype=np.float64)
    if data.shape[1] < 2:
        raise ValueError("待预测集至少需要 2 列: 密度, 含糖率")
    return data[:, :2]


def stable_sigmoid(z: np.ndarray) -> np.ndarray:
    probs = np.empty_like(z, dtype=np.float64)
    non_negative = z >= 0
    probs[non_negative] = 1.0 / (1.0 + np.exp(-z[non_negative]))
    exp_z = np.exp(z[~non_negative])
    probs[~non_negative] = exp_z / (1.0 + exp_z)
    return probs

def negative_log_likelihood(
    x_bias: np.ndarray,
    y: np.ndarray,
    weights: np.ndarray,
    regularization: float,
) -> float:
    probs = stable_sigmoid(x_bias @ weights)
    probs = np.clip(probs, 1e-12, 1.0 - 1e-12)
    data_loss = -np.sum(y * np.log(probs) + (1.0 - y) * np.log(1.0 - probs))
    reg_loss = 0.5 * regularization * np.sum(weights[1:] ** 2)
    return float(data_loss + reg_loss)


def train_logistic_regression_newton(
    x: np.ndarray,
    y: np.ndarray,
    max_iter: int,
    tol: float,
    regularization: float,
    line_search: bool,
) -> dict[str, np.ndarray | int]:
    sample_count, feature_count = x.shape
    x_bias = np.column_stack([np.ones(sample_count), x])
    weights = np.zeros(feature_count + 1, dtype=np.float64)

    loss_history: list[float] = []
    grad_norm_history: list[float] = []
    step_norm_history: list[float] = []

    iterations = max_iter

    for iteration in range(1, max_iter + 1):
        logits = x_bias @ weights
        probs = stable_sigmoid(logits)

        loss = negative_log_likelihood(x_bias, y, weights, regularization)
        gradient = x_bias.T @ (probs - y)
        gradient[1:] += regularization * weights[1:]

        curvature = probs * (1.0 - probs)
        hessian = x_bias.T @ (x_bias * curvature[:, None])
        hessian += np.diag([0.0] + [regularization] * feature_count)

        if np.linalg.cond(hessian) > 1e12:
            hessian += 1e-6 * np.eye(feature_count + 1, dtype=np.float64)

        step = np.linalg.solve(hessian, gradient)
        step_scale = 1.0

        if line_search:
            current_loss = loss
            while step_scale > 1e-4:
                candidate = weights - step_scale * step
                candidate_loss = negative_log_likelihood(
                    x_bias, y, candidate, regularization
                )
                if candidate_loss <= current_loss:
                    break
                step_scale *= 0.5

        weights = weights - step_scale * step

        updated_loss = negative_log_likelihood(x_bias, y, weights, regularization)
        grad_norm = float(np.linalg.norm(gradient))
        step_norm = float(np.linalg.norm(step_scale * step))

        loss_history.append(updated_loss)
        grad_norm_history.append(grad_norm)
        step_norm_history.append(step_norm)

        if grad_norm < tol or step_norm < tol:
            iterations = iteration
            break

    return {
        "weights": weights,
        "iterations": iterations,
        "loss_history": np.asarray(loss_history, dtype=np.float64),
        "grad_norm_history": np.asarray(grad_norm_history, dtype=np.float64),
        "step_norm_history": np.asarray(step_norm_history, dtype=np.float64),
    }


def predict_probabilities(weights: np.ndarray, x: np.ndarray) -> np.ndarray:
    x_bias = np.column_stack([np.ones(x.shape[0]), x])
    return stable_sigmoid(x_bias @ weights)


def predict_labels(weights: np.ndarray, x: np.ndarray, threshold: float = 0.5) -> np.ndarray:
    return (predict_probabilities(weights, x) >= threshold).astype(int)


def save_csv(path: Path, headers: list[str], rows: Iterable[Iterable[float | int]]) -> None:
    with path.open("w", encoding="utf-8-sig", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(headers)
        writer.writerows(rows)


def plot_decision_boundary(
    x_train: np.ndarray,
    y_train: np.ndarray,
    x_predict: np.ndarray,
    weights: np.ndarray,
    save_path: Path,
) -> None:
    plt.figure(figsize=(8, 6))
    positive_mask = y_train == 1
    negative_mask = ~positive_mask

    plt.scatter(
        x_train[positive_mask, 0],
        x_train[positive_mask, 1],
        s=70,
        c="#d83a3a",
        label="好瓜(训练集)",
    )
    plt.scatter(
        x_train[negative_mask, 0],
        x_train[negative_mask, 1],
        s=70,
        c="#2c5fd5",
        label="坏瓜(训练集)",
    )
    plt.scatter(
        x_predict[:, 0],
        x_predict[:, 1],
        s=45,
        marker="o",
        facecolors="none",
        edgecolors="#222222",
        label="4.0待预测样本",
    )

    if abs(weights[2]) > 1e-12:
        all_x = np.concatenate([x_train[:, 0], x_predict[:, 0]])
        x_span = np.linspace(all_x.min() - 0.02, all_x.max() + 0.02, 200)
        y_span = -(weights[0] + weights[1] * x_span) / weights[2]
        plt.plot(x_span, y_span, color="black", linewidth=1.8, label="决策边界")

    plt.xlabel("密度")
    plt.ylabel("含糖率")
    plt.title("手写对率回归分类结果与决策边界")
    plt.grid(True, linestyle="--", alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(save_path, dpi=150)
    plt.close()


def plot_prediction_results(
    x_train: np.ndarray,
    y_train: np.ndarray,
    x_predict: np.ndarray,
    predict_labels: np.ndarray,
    weights: np.ndarray,
    save_path: Path,
) -> None:
    plt.figure(figsize=(8, 6))

    positive_mask = y_train == 1
    negative_mask = ~positive_mask

    plt.scatter(
        x_train[positive_mask, 0],
        x_train[positive_mask, 1],
        s=70,
        c="#d83a3a",
        label="好瓜(训练集)",
    )

    plt.scatter(
        x_train[negative_mask, 0],
        x_train[negative_mask, 1],
        s=70,
        c="#2c5fd5",
        label="坏瓜(训练集)",
    )

    pred_positive = predict_labels == 1
    pred_negative = predict_labels == 0

    plt.scatter(
        x_predict[pred_positive, 0],
        x_predict[pred_positive, 1],
        s=80,
        marker="^",
        c="#ff8800",
        label="预测为好瓜",
    )

    plt.scatter(
        x_predict[pred_negative, 0],
        x_predict[pred_negative, 1],
        s=80,
        marker="^",
        c="#00aa55",
        label="预测为坏瓜",
    )

    if abs(weights[2]) > 1e-12:
        x_span = np.linspace(0, 1, 200)
        y_span = -(weights[0] + weights[1] * x_span) / weights[2]
        plt.plot(x_span, y_span, color="black", linewidth=2, label="决策边界")

    plt.xlabel("密度")
    plt.ylabel("含糖率")
    plt.title("对率回归预测结果可视化")
    plt.legend()
    plt.grid(True, linestyle="--", alpha=0.3)

    plt.tight_layout()
    plt.savefig(save_path, dpi=150)
    plt.close()


def main() -> None:
    args = parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)

    sample_ids, x_train, y_train = load_training_data(args.train_path)
    x_predict = load_predict_data(args.predict_path)

    model = train_logistic_regression_newton(
        x=x_train,
        y=y_train,
        max_iter=args.max_iter,
        tol=args.tol,
        regularization=args.regularization,
        line_search=not args.disable_line_search,
    )

    weights = model["weights"]
    train_probs = predict_probabilities(weights, x_train)
    train_labels = (train_probs >= 0.5).astype(int)
    predict_probs = predict_probabilities(weights, x_predict)
    predict_labels_array = (predict_probs >= 0.5).astype(int)

    train_accuracy = float(np.mean(train_labels == y_train))

    print("===== 对率回归实验结果 =====")
    print(f"迭代次数: {model['iterations']}")
    print(f"偏置项 b: {weights[0]:.6f}")
    print(f"权重 w1: {weights[1]:.6f}")
    print(f"权重 w2: {weights[2]:.6f}")
    print(f"最终损失: {model['loss_history'][-1]:.10f}")
    print(f"训练集准确率: {train_accuracy * 100:.2f}%")
    print(f"输出目录: {args.output_dir}")

    history_rows = zip(
        range(1, int(model["iterations"]) + 1),
        model["loss_history"],
        model["grad_norm_history"],
        model["step_norm_history"],
    )
    save_csv(
        args.output_dir / "training_history.csv",
        ["iteration", "loss", "grad_norm", "step_norm"],
        history_rows,
    )

    train_rows = zip(
        sample_ids,
        x_train[:, 0],
        x_train[:, 1],
        y_train.astype(int),
        train_probs,
        train_labels,
    )
    save_csv(
        args.output_dir / "train_predictions.csv",
        ["编号", "密度", "含糖率", "真实标签", "预测概率", "预测标签"],
        train_rows,
    )

    predict_rows = zip(
        range(1, x_predict.shape[0] + 1),
        x_predict[:, 0],
        x_predict[:, 1],
        predict_probs,
        predict_labels_array,
    )
    save_csv(
        args.output_dir / "predict_4_0.csv",
        ["编号", "密度", "含糖率", "预测概率", "预测标签"],
        predict_rows,
    )

    plot_decision_boundary(
        x_train=x_train,
        y_train=y_train,
        x_predict=x_predict,
        weights=weights,
        save_path=args.output_dir / "decision_boundary.png",
    )

    plot_prediction_results(
        x_train=x_train,
        y_train=y_train,
        x_predict=x_predict,
        predict_labels=predict_labels_array,
        weights=weights,
        save_path=args.output_dir / "prediction_result.png",
    )


if __name__ == "__main__":
    main()
