"""
实验三：决策树算法实践
—— 决策树分类与回归算法实现

分类任务：鸢尾花(Iris)数据集、葡萄酒(Wine)数据集
回归任务：加利福尼亚房价(California Housing)数据集、糖尿病(Diabetes)数据集
"""

import numpy as np
import matplotlib
matplotlib.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei']
matplotlib.rcParams['axes.unicode_minus'] = False
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from sklearn.datasets import (load_iris, load_wine, load_diabetes,
                              make_regression, load_linnerud)
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.tree import DecisionTreeClassifier, DecisionTreeRegressor, plot_tree
from sklearn.metrics import (accuracy_score, classification_report, confusion_matrix,
                             mean_squared_error, mean_absolute_error, r2_score)
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler

OUTPUT_DIR = r"C:\Users\yanha\Desktop\机器学习\实验报告\实验三"

# ============================================================
# 一、决策树分类 —— 鸢尾花数据集 (Iris)
# ============================================================
def classification_iris():
    print("=" * 60)
    print("一、决策树分类 — 鸢尾花(Iris)数据集")
    print("=" * 60)

    iris = load_iris()
    X, y = iris.data, iris.target
    feature_names = iris.feature_names
    target_names = iris.target_names

    # 使用前两个特征便于可视化决策边界
    X_2d = X[:, :2]
    X_train, X_test, y_train, y_test = train_test_split(
        X_2d, y, test_size=0.3, random_state=42)

    # 训练决策树
    clf = DecisionTreeClassifier(max_depth=3, random_state=42)
    clf.fit(X_train, y_train)

    # 预测与评估
    y_pred = clf.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    print(f"测试集准确率: {acc:.4f}")
    print(f"10折交叉验证平均准确率: {cross_val_score(clf, X_2d, y, cv=10).mean():.4f}")
    print("\n分类报告:")
    print(classification_report(y_test, y_pred, target_names=target_names))

    # 决策树可视化
    fig, ax = plt.subplots(figsize=(14, 10))
    plot_tree(clf, feature_names=feature_names[:2],
              class_names=target_names.tolist(), filled=True,
              rounded=True, fontsize=10, ax=ax)
    ax.set_title("鸢尾花数据集 — 决策树结构 (max_depth=3)", fontsize=14, fontweight='bold')
    fig.savefig(f"{OUTPUT_DIR}/fig1_iris_tree.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig1_iris_tree.png")

    # 决策边界图
    h = 0.02
    x_min, x_max = X_2d[:, 0].min() - 1, X_2d[:, 0].max() + 1
    y_min, y_max = X_2d[:, 1].min() - 1, X_2d[:, 1].max() + 1
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))
    Z = clf.predict(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)

    fig, ax = plt.subplots(figsize=(10, 7))
    cmap_light = ListedColormap(['#FFAAAA', '#AAFFAA', '#AAAAFF'])
    cmap_bold = ListedColormap(['#FF0000', '#00AA00', '#0000FF'])

    ax.contourf(xx, yy, Z, cmap=cmap_light, alpha=0.6)
    for i, name in enumerate(target_names):
        idx = np.where(y == i)
        ax.scatter(X_2d[idx, 0], X_2d[idx, 1], c=[cmap_bold(i)] * len(idx[0]),
                   label=name, edgecolor='k', s=50, alpha=0.8)
    ax.set_xlabel(feature_names[0], fontsize=12)
    ax.set_ylabel(feature_names[1], fontsize=12)
    ax.set_title("鸢尾花数据集 — 决策树分类决策边界 (前两个特征)", fontsize=14, fontweight='bold')
    ax.legend(loc='upper right', fontsize=10)
    fig.savefig(f"{OUTPUT_DIR}/fig2_iris_boundary.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig2_iris_boundary.png")

    # 特征重要性
    fig, ax = plt.subplots(figsize=(8, 5))
    importances = clf.feature_importances_
    indices = np.argsort(importances)[::-1]
    ax.bar(range(len(importances)), importances[indices], color=['#2ecc71', '#3498db'], edgecolor='black')
    ax.set_xticks(range(len(importances)))
    ax.set_xticklabels([feature_names[:2][i] for i in indices], fontsize=11)
    ax.set_ylabel("特征重要性", fontsize=12)
    ax.set_title("鸢尾花数据集 — 特征重要性", fontsize=14, fontweight='bold')
    for i, v in enumerate(importances[indices]):
        ax.text(i, v + 0.01, f'{v:.4f}', ha='center', fontsize=10)
    fig.savefig(f"{OUTPUT_DIR}/fig3_iris_importance.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig3_iris_importance.png")

    return clf


# ============================================================
# 二、决策树分类 —— 葡萄酒数据集 (Wine, UCI)
# ============================================================
def classification_wine():
    print("\n" + "=" * 60)
    print("二、决策树分类 — 葡萄酒(Wine, UCI)数据集")
    print("=" * 60)

    wine = load_wine()
    X, y = wine.data, wine.target
    feature_names = wine.feature_names
    target_names = wine.target_names

    # PCA 降维到2D用于可视化
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    pca = PCA(n_components=2)
    X_pca = pca.fit_transform(X_scaled)
    explained_var = pca.explained_variance_ratio_

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=42)

    clf = DecisionTreeClassifier(max_depth=3, random_state=42)
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    print(f"PCA解释方差比: PC1={explained_var[0]:.3f}, PC2={explained_var[1]:.3f}")
    print(f"测试集准确率: {acc:.4f}")
    print(f"10折交叉验证平均准确率: {cross_val_score(clf, X, y, cv=10).mean():.4f}")
    print("\n分类报告:")
    print(classification_report(y_test, y_pred, target_names=target_names))

    # 混淆矩阵
    cm = confusion_matrix(y_test, y_pred)
    fig, ax = plt.subplots(figsize=(8, 6))
    im = ax.imshow(cm, interpolation='nearest', cmap='Blues')
    ax.set_xticks(range(len(target_names)))
    ax.set_yticks(range(len(target_names)))
    ax.set_xticklabels(target_names, fontsize=11)
    ax.set_yticklabels(target_names, fontsize=11)
    ax.set_xlabel("预测类别", fontsize=12)
    ax.set_ylabel("真实类别", fontsize=12)
    ax.set_title("葡萄酒数据集 — 混淆矩阵", fontsize=14, fontweight='bold')
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            ax.text(j, i, str(cm[i, j]), ha='center', va='center',
                    fontsize=14, fontweight='bold',
                    color='white' if cm[i, j] > cm.max() / 2 else 'black')
    fig.colorbar(im, ax=ax)
    fig.savefig(f"{OUTPUT_DIR}/fig4_wine_confusion.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig4_wine_confusion.png")

    # 决策树结构可视化
    fig, ax = plt.subplots(figsize=(16, 10))
    plot_tree(clf, feature_names=feature_names,
              class_names=target_names.tolist(), filled=True,
              rounded=True, fontsize=8, ax=ax)
    ax.set_title("葡萄酒数据集 — 决策树结构 (max_depth=3)", fontsize=14, fontweight='bold')
    fig.savefig(f"{OUTPUT_DIR}/fig5_wine_tree.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig5_wine_tree.png")

    # 特征重要性
    fig, ax = plt.subplots(figsize=(10, 6))
    importances = clf.feature_importances_
    indices = np.argsort(importances)
    colors = plt.cm.RdYlGn(importances[indices] / importances.max())
    ax.barh(range(len(importances)), importances[indices], color=colors, edgecolor='black')
    ax.set_yticks(range(len(importances)))
    ax.set_yticklabels([feature_names[i] for i in indices], fontsize=9)
    ax.set_xlabel("特征重要性", fontsize=12)
    ax.set_title("葡萄酒数据集 — 特征重要性", fontsize=14, fontweight='bold')
    for i, v in enumerate(importances[indices]):
        ax.text(v + 0.01, i, f'{v:.4f}', va='center', fontsize=8)
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig6_wine_importance.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig6_wine_importance.png")

    # PCA 决策边界图
    # 在 PCA 空间训练新模型用于绘制决策边界
    clf_pca = DecisionTreeClassifier(max_depth=3, random_state=42)
    clf_pca.fit(X_pca, y)

    h = 0.05
    x_min, x_max = X_pca[:, 0].min() - 1, X_pca[:, 0].max() + 1
    y_min, y_max = X_pca[:, 1].min() - 1, X_pca[:, 1].max() + 1
    xx, yy = np.meshgrid(np.arange(x_min, x_max, h),
                         np.arange(y_min, y_max, h))
    Z = clf_pca.predict(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)

    fig, ax = plt.subplots(figsize=(10, 7))
    cmap_light = ListedColormap(['#FFAAAA', '#AAFFAA', '#AAAAFF'])
    cmap_bold = ListedColormap(['#FF0000', '#00AA00', '#0000FF'])

    ax.contourf(xx, yy, Z, cmap=cmap_light, alpha=0.6)
    for i, name in enumerate(target_names):
        idx = np.where(y == i)
        ax.scatter(X_pca[idx, 0], X_pca[idx, 1], c=[cmap_bold(i)] * len(idx[0]),
                   label=name, edgecolor='k', s=50, alpha=0.8)
    ax.set_xlabel(f"主成分 1 ({explained_var[0]:.1%} 方差)", fontsize=12)
    ax.set_ylabel(f"主成分 2 ({explained_var[1]:.1%} 方差)", fontsize=12)
    ax.set_title("葡萄酒数据集 (PCA降维) — 决策树分类决策边界", fontsize=14, fontweight='bold')
    ax.legend(loc='lower right', fontsize=10)
    fig.savefig(f"{OUTPUT_DIR}/fig7_wine_boundary.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig7_wine_boundary.png")

    return clf


# ============================================================
# 三、决策树回归 —— 合成房价数据集 (Simulated Housing)
# ============================================================
def regression_housing():
    print("\n" + "=" * 60)
    print("三、决策树回归 — 合成房价(Simulated Housing)数据集")
    print("=" * 60)

    # 生成模拟房价数据：8个特征，模拟房屋属性
    X, y = make_regression(
        n_samples=2000, n_features=8, noise=0.2,
        bias=200.0,  # 基础房价 (单位: $10k)
        random_state=42
    )
    feature_names = [
        "MedInc", "HouseAge", "AveRooms", "AveBedrms",
        "Population", "AveOccup", "Latitude", "Longitude"
    ]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=42)

    reg = DecisionTreeRegressor(max_depth=5, random_state=42)
    reg.fit(X_train, y_train)

    y_pred = reg.predict(X_test)

    mse = mean_squared_error(y_test, y_pred)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    rmse = np.sqrt(mse)

    print(f"MSE:  {mse:.4f}")
    print(f"RMSE: {rmse:.4f}")
    print(f"MAE:  {mae:.4f}")
    print(f"R^2:   {r2:.4f}")
    print(f"10折交叉验证 R^2: {cross_val_score(reg, X, y, cv=10, scoring='r2').mean():.4f}")

    # 预测值 vs 真实值散点图
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))

    ax = axes[0]
    ax.scatter(y_test, y_pred, alpha=0.4, edgecolors='k', linewidth=0.3, c='steelblue')
    ax.plot([y_test.min(), y_test.max()], [y_test.min(), y_test.max()],
            'r--', linewidth=2, label='y = x (理想预测)')
    ax.set_xlabel("真实房价 (单位: $10k)", fontsize=12)
    ax.set_ylabel("预测房价 (单位: $10k)", fontsize=12)
    ax.set_title("合成房价数据集 — 预测值 vs 真实值", fontsize=13, fontweight='bold')
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)

    ax = axes[1]
    residuals = y_test - y_pred
    ax.scatter(y_pred, residuals, alpha=0.4, edgecolors='k', linewidth=0.3, c='coral')
    ax.axhline(y=0, color='r', linestyle='--', linewidth=2)
    ax.set_xlabel("预测房价", fontsize=12)
    ax.set_ylabel("残差 (真实值 - 预测值)", fontsize=12)
    ax.set_title("合成房价数据集 — 残差分布图", fontsize=13, fontweight='bold')
    ax.grid(True, alpha=0.3)

    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig8_housing_prediction.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig8_housing_prediction.png")

    # 特征重要性
    fig, ax = plt.subplots(figsize=(10, 6))
    importances = reg.feature_importances_
    indices = np.argsort(importances)
    colors = plt.cm.viridis(importances[indices] / importances.max())
    ax.barh(range(len(importances)), importances[indices], color=colors, edgecolor='black')
    ax.set_yticks(range(len(importances)))
    ax.set_yticklabels([feature_names[i] for i in indices], fontsize=9)
    ax.set_xlabel("特征重要性", fontsize=12)
    ax.set_title("合成房价数据集 — 特征重要性", fontsize=14, fontweight='bold')
    for i, v in enumerate(importances[indices]):
        ax.text(v + 0.01, i, f'{v:.4f}', va='center', fontsize=8)
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig9_housing_importance.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig9_housing_importance.png")

    # 决策树结构 (max_depth=3 便于展示)
    reg_vis = DecisionTreeRegressor(max_depth=3, random_state=42)
    reg_vis.fit(X_train, y_train)

    fig, ax = plt.subplots(figsize=(18, 10))
    plot_tree(reg_vis, feature_names=feature_names, filled=True,
              rounded=True, fontsize=8, ax=ax)
    ax.set_title("合成房价数据集 — 决策回归树结构 (max_depth=3)", fontsize=14, fontweight='bold')
    fig.savefig(f"{OUTPUT_DIR}/fig10_housing_tree.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig10_housing_tree.png")

    return reg


# ============================================================
# 四、决策树回归 —— 糖尿病数据集 (Diabetes, UCI)
# ============================================================
def regression_diabetes():
    print("\n" + "=" * 60)
    print("四、决策树回归 — 糖尿病(Diabetes, UCI)数据集")
    print("=" * 60)

    diabetes = load_diabetes()
    X, y = diabetes.data, diabetes.target
    feature_names = diabetes.feature_names

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, random_state=42)

    reg = DecisionTreeRegressor(max_depth=4, random_state=42)
    reg.fit(X_train, y_train)

    y_pred = reg.predict(X_test)

    mse = mean_squared_error(y_test, y_pred)
    mae = mean_absolute_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)
    rmse = np.sqrt(mse)

    print(f"MSE:  {mse:.4f}")
    print(f"RMSE: {rmse:.4f}")
    print(f"MAE:  {mae:.4f}")
    print(f"R^2:   {r2:.4f}")
    print(f"10折交叉验证 R^2: {cross_val_score(reg, X, y, cv=10, scoring='r2').mean():.4f}")

    # 综合图：预测值 vs 真实值 + 残差
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))

    ax = axes[0]
    ax.scatter(y_test, y_pred, alpha=0.4, edgecolors='k', linewidth=0.3, c='teal')
    ax.plot([y_test.min(), y_test.max()], [y_test.min(), y_test.max()],
            'r--', linewidth=2, label='y = x')
    ax.set_xlabel("真实疾病进展值", fontsize=12)
    ax.set_ylabel("预测疾病进展值", fontsize=12)
    ax.set_title("糖尿病数据集 — 预测值 vs 真实值", fontsize=13, fontweight='bold')
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)

    ax = axes[1]
    residuals = y_test - y_pred
    ax.scatter(y_pred, residuals, alpha=0.4, edgecolors='k', linewidth=0.3, c='darkorange')
    ax.axhline(y=0, color='r', linestyle='--', linewidth=2)
    ax.set_xlabel("预测疾病进展值", fontsize=12)
    ax.set_ylabel("残差", fontsize=12)
    ax.set_title("糖尿病数据集 — 残差分布图", fontsize=13, fontweight='bold')
    ax.grid(True, alpha=0.3)

    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig11_diabetes_prediction.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig11_diabetes_prediction.png")

    # 特征重要性
    fig, ax = plt.subplots(figsize=(10, 6))
    importances = reg.feature_importances_
    indices = np.argsort(importances)
    colors = plt.cm.plasma(importances[indices] / importances.max())
    ax.barh(range(len(importances)), importances[indices], color=colors, edgecolor='black')
    ax.set_yticks(range(len(importances)))
    ax.set_yticklabels([feature_names[i] for i in indices], fontsize=9)
    ax.set_xlabel("特征重要性", fontsize=12)
    ax.set_title("糖尿病数据集 — 特征重要性", fontsize=14, fontweight='bold')
    for i, v in enumerate(importances[indices]):
        ax.text(v + 0.01, i, f'{v:.4f}', va='center', fontsize=8)
    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig12_diabetes_importance.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig12_diabetes_importance.png")

    # 决策树结构
    fig, ax = plt.subplots(figsize=(16, 10))
    plot_tree(reg, feature_names=feature_names, filled=True,
              rounded=True, fontsize=9, ax=ax)
    ax.set_title("糖尿病数据集 — 决策回归树结构 (max_depth=4)", fontsize=14, fontweight='bold')
    fig.savefig(f"{OUTPUT_DIR}/fig13_diabetes_tree.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig13_diabetes_tree.png")

    return reg


# ============================================================
# 五、综合对比与分析
# ============================================================
def comprehensive_analysis():
    """综合对比不同深度参数对模型的影响"""
    print("\n" + "=" * 60)
    print("五、综合对比分析")
    print("=" * 60)

    # --- 分类：不同 max_depth 对准确率的影响 ---
    iris = load_iris()
    wine = load_wine()

    depths = range(1, 16)
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))

    for ax, (X, y, name) in zip(axes, [
        (iris.data, iris.target, "鸢尾花"),
        (wine.data, wine.target, "葡萄酒 (Wine)")
    ]):
        train_scores, test_scores = [], []
        for d in depths:
            clf = DecisionTreeClassifier(max_depth=d, random_state=42)
            X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=42)
            clf.fit(X_tr, y_tr)
            train_scores.append(accuracy_score(y_tr, clf.predict(X_tr)))
            test_scores.append(accuracy_score(y_te, clf.predict(X_te)))

        ax.plot(depths, train_scores, 'o-', color='#3498db', linewidth=2,
                markersize=6, label='训练集准确率')
        ax.plot(depths, test_scores, 's-', color='#e74c3c', linewidth=2,
                markersize=6, label='测试集准确率')
        ax.set_xlabel("max_depth", fontsize=12)
        ax.set_ylabel("准确率", fontsize=12)
        ax.set_title(f"{name} — max_depth 对准确率的影响", fontsize=13, fontweight='bold')
        ax.legend(fontsize=10)
        ax.grid(True, alpha=0.3)

    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig14_depth_accuracy.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig14_depth_accuracy.png")

    # --- 回归：不同 max_depth 对 R^2 的影响 ---
    housing_X, housing_y = make_regression(
        n_samples=2000, n_features=8, noise=0.2, bias=200.0, random_state=42)
    diabetes = load_diabetes()

    depths = range(1, 21)
    fig, axes = plt.subplots(1, 2, figsize=(14, 6))

    for ax, (X, y, name) in zip(axes, [
        (housing_X, housing_y, "合成房价"),
        (diabetes.data, diabetes.target, "糖尿病")
    ]):
        train_scores, test_scores = [], []
        for d in depths:
            reg = DecisionTreeRegressor(max_depth=d, random_state=42)
            X_tr, X_te, y_tr, y_te = train_test_split(X, y, test_size=0.3, random_state=42)
            reg.fit(X_tr, y_tr)
            train_scores.append(r2_score(y_tr, reg.predict(X_tr)))
            test_scores.append(r2_score(y_te, reg.predict(X_te)))

        ax.plot(depths, train_scores, 'o-', color='#2ecc71', linewidth=2,
                markersize=6, label='训练集 R^2')
        ax.plot(depths, test_scores, 's-', color='#e67e22', linewidth=2,
                markersize=6, label='测试集 R^2')
        ax.set_xlabel("max_depth", fontsize=12)
        ax.set_ylabel("R^2", fontsize=12)
        ax.set_title(f"{name} — max_depth 对 R^2 的影响", fontsize=13, fontweight='bold')
        ax.legend(fontsize=10)
        ax.grid(True, alpha=0.3)
        ax.axhline(y=0, color='gray', linestyle=':', alpha=0.7)

    fig.tight_layout()
    fig.savefig(f"{OUTPUT_DIR}/fig15_depth_r2.png", dpi=150, bbox_inches='tight')
    plt.close()
    print("→ 已保存: fig15_depth_r2.png")


# ============================================================
# 主程序入口
# ============================================================
if __name__ == "__main__":
    print("实验三：决策树算法实践\n")
    print("算法说明：")
    print("  决策树分类 — 基于信息增益(CART: Gini系数)选择最优划分特征，递归构建树")
    print("  决策树回归 — 基于MSE最小化选择划分特征和划分点，叶子节点输出样本均值")
    print("  使用 sklearn.tree 中的 DecisionTreeClassifier 和 DecisionTreeRegressor 实现\n")

    classification_iris()
    classification_wine()
    regression_housing()
    regression_diabetes()
    comprehensive_analysis()

    print("\n" + "=" * 60)
    print("所有实验完成！输出图片共 15 张，保存在：")
    print(f"  {OUTPUT_DIR}")
    print("=" * 60)
