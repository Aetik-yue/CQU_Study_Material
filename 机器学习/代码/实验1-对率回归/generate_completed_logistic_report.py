from __future__ import annotations

import struct
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
OUTPUT_PATH = BASE_DIR / "实验1-对率回归算法实践-已完成.rtf"

IMAGES = {
    "wm_boundary": BASE_DIR / "logistic_regression_python" / "outputs" / "decision_boundary.png",
    "wm_pred": BASE_DIR / "logistic_regression_python" / "outputs" / "prediction_result.png",
    "iris_curve": BASE_DIR / "logistic_regression_python" / "iris_outputs" / "02_training_curves.png",
    "iris_boundary": BASE_DIR / "logistic_regression_python" / "iris_outputs" / "03_decision_boundary.png",
    "iris_cm": BASE_DIR / "iris_multiclass_outputs" / "confusion_matrix.png",
}


def rtf_escape(text: str) -> str:
    pieces: list[str] = []
    for ch in text:
        code = ord(ch)
        if ch == "\\":
            pieces.append(r"\\")
        elif ch == "{":
            pieces.append(r"\{")
        elif ch == "}":
            pieces.append(r"\}")
        elif ch == "\n":
            pieces.append(r"\line ")
        elif 32 <= code < 127:
            pieces.append(ch)
        else:
            signed = code if code < 32768 else code - 65536
            pieces.append(f"\\u{signed}?")
    return "".join(pieces)


def para(
    text: str = "",
    *,
    fs: int = 24,
    bold: bool = False,
    align: str = "left",
    after: int = 120,
    before: int = 0,
    indent: int = 0,
) -> str:
    align_map = {"left": r"\ql", "center": r"\qc", "right": r"\qr"}
    parts = [
        r"{\pard",
        align_map.get(align, r"\ql"),
        f"\\sb{before}",
        f"\\sa{after}",
        f"\\li{indent}",
        r"\f0",
        f"\\fs{fs}",
    ]
    if bold:
        parts.append(r"\b")
    parts.append(" ")
    parts.append(rtf_escape(text))
    if bold:
        parts.append(r"\b0")
    parts.append(r"\par}")
    return "".join(parts)


def png_size(path: Path) -> tuple[int, int]:
    with path.open("rb") as file:
        if file.read(8) != b"\x89PNG\r\n\x1a\n":
            raise ValueError(f"Not a PNG file: {path}")
        _length = struct.unpack(">I", file.read(4))[0]
        chunk = file.read(4)
        if chunk != b"IHDR":
            raise ValueError(f"Invalid PNG header: {path}")
        width, height = struct.unpack(">II", file.read(8))
    return width, height


def image_block(path: Path, caption: str, width_inches: float = 5.8) -> list[str]:
    width, height = png_size(path)
    data = path.read_bytes().hex().upper()
    picwgoal = int(width_inches * 1440)
    pichgoal = int(picwgoal * height / width)
    hex_lines = "\n".join(data[i : i + 128] for i in range(0, len(data), 128))
    pict = (
        r"{\pard\qc\sa120{\pict\pngblip"
        + f"\\picw{width}\\pich{height}\\picwgoal{picwgoal}\\pichgoal{pichgoal}\n"
        + hex_lines
        + "\n}\\par}"
    )
    return [pict, para(caption, fs=20, align="center", after=180)]


def main() -> None:
    lines: list[str] = []
    lines.append(r"{\rtf1\ansi\ansicpg936\deff0\uc1")
    lines.append(
        r"{\fonttbl{\f0\fnil\fcharset134 Microsoft YaHei;}{\f1\fnil\fcharset0 Calibri;}{\f2\fmodern\fcharset0 Consolas;}}"
    )
    lines.append(r"{\colortbl;\red0\green0\blue0;\red47\green84\blue150;\red102\green102\blue102;}")
    lines.append(r"\viewkind4\paperw11906\paperh16838\margl1440\margr1440\margt1440\margb1440")

    lines.append(para("《机器学习》实验报告", fs=32, bold=True, align="center", after=180))
    lines.append(para("对数几率回归算法实践", fs=28, bold=True, align="center", after=120))
    lines.append(para("实验日期：2026年4月12日    实验类型：设计性实验", fs=22, align="center", after=200))
    lines.append(
        para(
            "姓名：________    学号：________    年级/专业/班级：________________",
            fs=22,
            align="center",
            after=260,
        )
    )

    lines.append(para("一、实验目的", fs=26, bold=True, after=140))
    for text in [
        "1. 掌握线性模型与对数几率回归(Logistic Regression)的基本思想，理解 Sigmoid 函数、交叉熵损失以及参数求解过程。",
        "2. 在不调用现成分类模型训练函数的前提下，手写实现对率回归训练器、预测器和结果可视化模块。",
        "3. 将算法应用到西瓜数据集与鸢尾花数据集，观察模型在二分类和多分类任务中的表现，并分析误差来源。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(para("二、实验内容", fs=26, bold=True, after=140))
    for text in [
        "1. 阅读并整理对率回归的数学原理，明确概率建模、损失函数与参数更新之间的关系。",
        "2. 基于 Python 手写 Newton 法对率回归程序，对西瓜 3.0a 数据集进行训练，并对 4.0 数据集进行预测。",
        "3. 使用鸢尾花数据集完成二分类实验(setosa 与 versicolor)；在此基础上进一步完成一对多(One-vs-Rest)三分类扩展实验。",
        "4. 输出训练过程记录、预测结果、决策边界图、训练曲线图和混淆矩阵，并据此完成实验分析与总结。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(para("三、实验原理", fs=26, bold=True, after=140))
    for text in [
        "1. 对率回归首先对输入特征进行线性组合 z = w^T x + b，然后通过 p(y=1|x)=1/(1+exp(-z)) 将结果压缩到 0 到 1 区间，用来表示样本属于正类的概率。",
        "2. 训练阶段最小化带 L2 正则项的交叉熵损失：L = -Σ[y_i ln p_i + (1-y_i) ln(1-p_i)] + (λ/2)||w||^2。该目标函数既能反映分类误差，也能抑制参数过大导致的过拟合。",
        "3. 西瓜实验采用手写 Newton 法求解参数。每轮迭代先计算梯度 g 与 Hessian 矩阵 H，再执行 θ_{t+1}=θ_t-H^{-1}g，并配合回溯线搜索提升数值稳定性。",
        "4. 鸢尾花二分类实验使用梯度下降训练；三分类扩展实验采用 One-vs-Rest 策略，分别训练 setosa、versicolor、virginica 三个二分类器，最后选择得分最高的类别作为预测结果。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(para("四、实验环境与数据集", fs=26, bold=True, after=140))
    for text in [
        "1. 实验环境：Windows 平台，Python 3.12，NumPy，Matplotlib；同时保留了 MATLAB 版本源码用于对照。",
        "2. 西瓜数据集：训练集采用 3.0a.csv，共 17 个带标签样本，特征为“密度”和“含糖率”，类别为“好瓜/坏瓜”；预测集采用 4.0.csv，共 30 个待分类样本。",
        "3. 鸢尾花二分类数据集：从 iris.csv 中选取 setosa 与 versicolor 两类，共 100 个样本，使用前两个特征进行可视化，并按 8:2 划分训练集与测试集。",
        "4. 鸢尾花三分类扩展：使用四个特征完成 setosa、versicolor、virginica 三分类，以检验手写对率回归在多分类场景中的适用性。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(para("五、实验过程与核心实现说明", fs=26, bold=True, after=140))
    for text in [
        "1. 数据读取模块兼容 utf-8-sig、gb18030、gbk 等编码，能够自动跳过空行和非数值表头，保证不同 CSV 文件都能稳定载入。",
        "2. 西瓜实验核心训练程序为 logistic_regression_from_scratch.py：手写 stable sigmoid、负对数似然、梯度、Hessian、回溯线搜索和 CSV/PNG 结果导出逻辑。",
        "3. 鸢尾花二分类程序为 iris_logistic_regression.py：先进行标准化，再执行梯度下降迭代，同时记录损失与准确率曲线，便于观察收敛过程。",
        "4. MATLAB 目录中的 run_experiment.m、train_logistic_regression_newton.m 与 predict_logistic_regression.m 实现了同样的训练—预测—绘图流程，可作为 Python 实现的对照版本。",
        "5. 本次报告在原有源码基础上，重点补充了实验结果、误分类分析、扩展实验与总结，使报告内容更加完整。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(para("六、实验结果与分析", fs=26, bold=True, after=140))
    lines.append(para("（一）西瓜数据集二分类结果", fs=24, bold=True, after=120))
    for text in [
        "1. 训练配置：Newton 最大迭代次数 50，停止阈值 1e-8，L2 正则化强度 1e-6，并启用回溯线搜索。",
        "2. 训练结果：模型在第 6 次迭代收敛，得到参数 b=-3.264160，w1=0.603198，w2=14.451581，最终损失为 7.9646802631。",
        "3. 训练集性能：TP=7，TN=8，FP=1，FN=1，准确率 Accuracy=88.24%，精确率 Precision=87.50%，召回率 Recall=87.50%，F1=87.50%。",
        "4. 误分类样本共有 2 个：编号 7 的好瓜被判为坏瓜，其预测概率仅为 0.3056；编号 15 的坏瓜被判为好瓜，其预测概率高达 0.9089。说明该小规模数据集在局部区域存在明显重叠，单一线性分界面对边界样本较敏感。",
        "5. 对 4.0 待预测数据集的 30 个样本进行分类后，其中预测为好瓜 21 个，预测为坏瓜 9 个；被判为坏瓜的样本编号为 7、9、11、12、13、14、16、17、18。",
        "6. 从决策边界图可以看出，模型主要依赖“含糖率”方向进行区分，说明该特征在当前数据上的判别作用更强。",
    ]:
        lines.append(para(text, fs=22, indent=280))
    lines.extend(image_block(IMAGES["wm_boundary"], "图1 西瓜数据集训练样本与决策边界图", width_inches=5.7))
    lines.extend(image_block(IMAGES["wm_pred"], "图2 西瓜 4.0 数据集预测结果可视化", width_inches=5.7))

    lines.append(para("（二）鸢尾花二分类结果（setosa vs versicolor）", fs=24, bold=True, after=120))
    for text in [
        "1. 数据情况：共 100 个样本，类别分布均衡；选取前两个特征，先标准化后训练，其中训练集 80 个样本，测试集 20 个样本。",
        "2. 训练过程：学习率 0.5，迭代 500 次，正则化强度 0.01。损失值从 0.693147 持续下降到约 0.086241，准确率很快提升到 1.0000。",
        "3. 最终结果：训练集准确率 100%，测试集准确率 100%，精确率、召回率和 F1 分数均为 1.0000。标准化特征空间上的决策边界方程为 -1.1339*x1 + 2.9922*x2 + 0.1899 = 0。",
        "4. 分析：setosa 与 versicolor 在所选特征上可分性较强，因此即使只使用两个特征，线性模型也能取得非常理想的分类效果。",
    ]:
        lines.append(para(text, fs=22, indent=280))
    lines.extend(image_block(IMAGES["iris_curve"], "图3 鸢尾花二分类训练曲线", width_inches=5.6))
    lines.extend(image_block(IMAGES["iris_boundary"], "图4 鸢尾花二分类决策边界图", width_inches=5.8))

    lines.append(para("（三）鸢尾花三分类扩展结果（One-vs-Rest）", fs=24, bold=True, after=120))
    for text in [
        "1. 扩展实验使用四维特征分别训练 setosa、versicolor、virginica 三个二分类器，并通过比较三个分类器输出得分完成最终类别判定。",
        "2. 测试集混淆矩阵结果为：setosa 10/10 分类正确，versicolor 9 个样本中 7 个分类正确，virginica 11 个样本中 10 个分类正确，整体准确率为 90.00%。",
        "3. 错分样本共 3 个，全部出现在 versicolor 与 virginica 之间。其中两个 versicolor 被判为 virginica，一个 virginica 被判为 versicolor，说明这两类在花瓣长度和花瓣宽度上存在一定重叠。",
        "4. 与 setosa 相比，versicolor 和 virginica 的分布更接近，因此多分类场景下线性模型的区分难度更大；若继续提高精度，可以考虑增加特征工程、调整正则项或引入非线性模型。",
    ]:
        lines.append(para(text, fs=22, indent=280))
    lines.extend(image_block(IMAGES["iris_cm"], "图5 鸢尾花三分类混淆矩阵", width_inches=4.6))

    lines.append(para("七、实验总结", fs=26, bold=True, after=140))
    for text in [
        "1. 本次实验完成了对数几率回归从原理理解、代码实现到结果分析的完整流程，进一步加深了我对线性分类模型的认识。",
        "2. 通过手写 Sigmoid、损失函数、梯度、Hessian 与参数更新过程，我对“模型为什么能收敛、为什么会错分边界样本”有了更直观的理解。",
        "3. 西瓜数据集实验表明：在小样本且类别边界存在重叠时，线性模型能够获得较好效果，但仍会受到样本分布和线性可分性的限制。",
        "4. 鸢尾花实验说明：当特征具有较好区分度时，对率回归能够以较低复杂度取得很高精度；而在三分类扩展中，versicolor 与 virginica 的混淆也提醒我们，模型能力与数据可分性是相互制约的。",
        "5. 后续可以继续从三方面改进：一是系统比较梯度下降与 Newton 法的收敛速度；二是补充更多评价指标与交叉验证；三是尝试核方法或神经网络等非线性模型进行对比。",
    ]:
        lines.append(para(text, fs=22, indent=280))

    lines.append(
        para(
            "附：本报告对应的主要程序与结果文件位于实验目录下，包括 logistic_regression_from_scratch.py、iris_logistic_regression.py、outputs/、iris_outputs/ 与 iris_multiclass_outputs/。",
            fs=20,
            after=120,
        )
    )
    lines.append("}")

    OUTPUT_PATH.write_text("\n".join(lines), encoding="ascii")
    print(OUTPUT_PATH)
    print(OUTPUT_PATH.stat().st_size)


if __name__ == "__main__":
    main()
