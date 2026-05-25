"""
实验4：聚类算法实践
- K-means聚类（原型聚类）
- DBSCAN聚类（密度聚类）
数据集：Iris鸢尾花数据集
K值：2, 3, 4, 5
每个K值使用3组不同随机初始中心点
"""
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from sklearn.cluster import KMeans, DBSCAN
from sklearn.metrics import silhouette_score, davies_bouldin_score, adjusted_rand_score
from sklearn.preprocessing import StandardScaler
from sklearn.decomposition import PCA
import warnings
warnings.filterwarnings('ignore')

# 设置中文字体
plt.rcParams['font.sans-serif'] = ['SimHei', 'Microsoft YaHei', 'DejaVu Sans']
plt.rcParams['axes.unicode_minus'] = False

OUTPUT_DIR = "../../输出结果/实验4-clustering_outputs/"

# ========== 1. 加载数据 ==========
df = pd.read_csv("../../数据集/实验一数据集/数据集/iris.csv", index_col=0)
df.columns = ['Sepal_Length', 'Sepal_Width', 'Petal_Length', 'Petal_Width', 'Species']
X = df[['Sepal_Length', 'Sepal_Width', 'Petal_Length', 'Petal_Width']].values
y_true = df['Species'].values

# 标准化（K-means对尺度敏感）
scaler = StandardScaler()
X_scaled = scaler.fit_transform(X)

# 为可视化方便，取前两个特征和PCA降维
X_2d = X_scaled[:, :2]  # 使用前两个特征用于决策边界可视化
pca = PCA(n_components=2)
X_pca = pca.fit_transform(X_scaled)

species_names = ['setosa', 'versicolor', 'virginica']
colors_true = ['#FF6B6B', '#4ECDC4', '#45B7D1']

print("=" * 60)
print("实验4：聚类算法实践")
print("=" * 60)
print(f"数据集: Iris鸢尾花数据集")
print(f"样本数: {X.shape[0]}, 特征数: {X.shape[1]}")
print(f"真实类别: {len(np.unique(y_true))} 类")
print()

# ========== 2. K-means实验 ==========
K_VALUES = [2, 3, 4, 5]
N_INIT = 3  # 每个K值3组不同初始中心点

results = {}

print("=" * 60)
print("K-means 聚类实验")
print("=" * 60)

for k in K_VALUES:
    print(f"\n--- K = {k} ---")
    results[k] = []

    for init_idx in range(N_INIT):
        random_state = init_idx * 42 + k * 7
        km = KMeans(n_clusters=k, random_state=random_state, n_init=1, init='random')
        y_pred = km.fit_predict(X_scaled)

        inertia = km.inertia_
        sil = silhouette_score(X_scaled, y_pred)
        db = davies_bouldin_score(X_scaled, y_pred)

        results[k].append({
            'model': km,
            'labels': y_pred,
            'inertia': inertia,
            'silhouette': sil,
            'db_index': db,
            'random_state': random_state,
            'centroids': km.cluster_centers_
        })

        print(f"  初始中心点{init_idx+1} (random_state={random_state}):")
        print(f"    SSE/Inertia = {inertia:.3f}")
        print(f"    Silhouette = {sil:.4f}")
        print(f"    Davies-Bouldin = {db:.4f}")
        print(f"    聚类分布: {np.bincount(y_pred)}")

# 汇总对比表
print("\n" + "=" * 60)
print("K-means 结果汇总")
print("=" * 60)
print(f"{'K':<4} {'初始组':<6} {'SSE':<12} {'Silhouette':<12} {'Davies-Bouldin':<15}")
print("-" * 55)
for k in K_VALUES:
    for idx in range(N_INIT):
        r = results[k][idx]
        print(f"{k:<4} {idx+1:<6} {r['inertia']:<12.3f} {r['silhouette']:<12.4f} {r['db_index']:<15.4f}")

# ========== 3. DBSCAN实验 ==========
print("\n" + "=" * 60)
print("DBSCAN 密度聚类实验")
print("=" * 60)

# 尝试不同的 eps 和 min_samples 参数
dbscan_params = [
    (0.5, 5),
    (0.7, 5),
    (0.9, 5),
    (1.1, 5),
]

for eps, min_samples in dbscan_params:
    db = DBSCAN(eps=eps, min_samples=min_samples)
    y_db = db.fit_predict(X_scaled)
    n_clusters = len(set(y_db)) - (1 if -1 in y_db else 0)
    n_noise = np.sum(y_db == -1)

    print(f"\neps={eps}, min_samples={min_samples}:")
    print(f"  簇数: {n_clusters}, 噪声点数: {n_noise}")
    print(f"  聚类分布: {np.bincount(y_db + 1)}")

    if n_clusters >= 2:
        mask = y_db != -1
        if np.sum(mask) > n_clusters:
            sil = silhouette_score(X_scaled[mask], y_db[mask])
            db_idx = davies_bouldin_score(X_scaled[mask], y_db[mask])
            print(f"  Silhouette (排除噪声): {sil:.4f}")
            print(f"  Davies-Bouldin (排除噪声): {db_idx:.4f}")

# ========== 4. 绘制聚类结果图 ==========
print("\n生成可视化图表...")

# 4a. 真实标签
fig, axes = plt.subplots(1, 2, figsize=(14, 6))
for i, (name, color) in enumerate(zip(species_names, colors_true)):
    mask = y_true == name
    axes[0].scatter(X_pca[mask, 0], X_pca[mask, 1], c=color, label=name, s=20, alpha=0.7)
axes[0].set_title('(a) 真实类别分布 (PCA)', fontsize=13, fontweight='bold')
axes[0].legend(fontsize=9)
axes[0].grid(True, alpha=0.3)

for i, (name, color) in enumerate(zip(species_names, colors_true)):
    mask = y_true == name
    axes[1].scatter(X_2d[mask, 0], X_2d[mask, 1], c=color, label=name, s=20, alpha=0.7)
axes[1].set_title('(b) 真实类别分布 (前2特征)', fontsize=13, fontweight='bold')
axes[1].legend(fontsize=9)
axes[1].grid(True, alpha=0.3)

plt.suptitle('图0：Iris数据集真实类别分布', fontsize=14, fontweight='bold')
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig0_true_labels.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig0_true_labels.png")

# 4b. 不同K值的K-means聚类结果 (PCA) - 单独图表
colors_k = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#F7DC6F', '#BB8FCE', '#F0B27A']
fig, axes = plt.subplots(2, 2, figsize=(14, 14))
for idx, k in enumerate(K_VALUES):
    ax = axes[idx // 2, idx % 2]
    best = max(results[k], key=lambda x: x['silhouette'])
    y_k = best['labels']
    for c in range(k):
        mask = y_k == c
        ax.scatter(X_pca[mask, 0], X_pca[mask, 1], c=colors_k[c], s=20, alpha=0.7, edgecolors='k', linewidth=0.3)
    centroids_pca = pca.transform(best['centroids'])
    ax.scatter(centroids_pca[:, 0], centroids_pca[:, 1], c='red', marker='X', s=200, edgecolors='black', linewidth=1.5, zorder=10)
    ax.set_title(f'K-means K={k} (最佳Silhouette={best["silhouette"]:.3f})', fontsize=12, fontweight='bold')
    ax.grid(True, alpha=0.3)

plt.suptitle('图1：Iris数据集 K-means聚类结果 (PCA降维)', fontsize=15, fontweight='bold', y=1.01)
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig1_kmeans_pca_results.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig1_kmeans_pca_results.png")

# 4c. 每个K值3组初始化对比
fig, axes = plt.subplots(len(K_VALUES), N_INIT + 1, figsize=(20, 22))
for i, k in enumerate(K_VALUES):
    # 第一列：真实分布
    for j, (name, color) in enumerate(zip(species_names, colors_true)):
        mask = y_true == name
        axes[i, 0].scatter(X_pca[mask, 0], X_pca[mask, 1], c=color, label=name, s=10, alpha=0.7)
    axes[i, 0].set_title(f'真实分布', fontsize=11, fontweight='bold')
    axes[i, 0].set_ylabel(f'K={k}', fontsize=14, fontweight='bold')
    axes[i, 0].grid(True, alpha=0.3)
    if i == 0:
        axes[i, 0].legend(fontsize=7, loc='upper right')

    for j in range(N_INIT):
        r = results[k][j]
        y_k = r['labels']
        ax = axes[i, j + 1]
        for c in range(k):
            mask = y_k == c
            ax.scatter(X_pca[mask, 0], X_pca[mask, 1], c=colors_k[c], s=10, alpha=0.7, edgecolors='k', linewidth=0.2)
        centroids_pca = pca.transform(r['centroids'])
        ax.scatter(centroids_pca[:, 0], centroids_pca[:, 1], c='red', marker='X', s=100, edgecolors='black', linewidth=1, zorder=10)
        ax.set_title(f'init{j+1} Sil={r["silhouette"]:.3f}\nSSE={r["inertia"]:.1f}', fontsize=10)
        ax.grid(True, alpha=0.3)

plt.suptitle('图2：不同K值和初始中心点的K-means聚类对比 (PCA)', fontsize=15, fontweight='bold', y=1.01)
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig2_kmeans_all_inits.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig2_kmeans_all_inits.png")

# 4d. 决策边界图 (使用前两个特征)
x_min, x_max = X_2d[:, 0].min() - 0.5, X_2d[:, 0].max() + 0.5
y_min, y_max = X_2d[:, 1].min() - 0.5, X_2d[:, 1].max() + 0.5
xx, yy = np.meshgrid(np.linspace(x_min, x_max, 300), np.linspace(y_min, y_max, 300))

fig, axes = plt.subplots(2, 2, figsize=(16, 14))
for idx, k in enumerate(K_VALUES):
    ax = axes[idx // 2, idx % 2]
    best = max(results[k], key=lambda x: x['silhouette'])

    # 用X_2d重新训练
    km_2d = KMeans(n_clusters=k, random_state=best['random_state'], n_init=1)
    km_2d.fit(X_2d)
    Z = km_2d.predict(np.c_[xx.ravel(), yy.ravel()])
    Z = Z.reshape(xx.shape)

    cmap_light = ListedColormap(['#FFCCCC', '#CCEEEE', '#CCDDEE', '#FFFFCC', '#EECCFF'][:k])
    ax.contourf(xx, yy, Z, alpha=0.3, cmap=cmap_light)

    y_k = best['labels']
    for c in range(k):
        mask = y_k == c
        ax.scatter(X_2d[mask, 0], X_2d[mask, 1], c=colors_k[c], s=25, alpha=0.8, edgecolors='k', linewidth=0.5)
    ax.scatter(km_2d.cluster_centers_[:, 0], km_2d.cluster_centers_[:, 1],
               c='red', marker='X', s=250, edgecolors='black', linewidth=2, zorder=10)
    ax.set_title(f'K = {k}', fontsize=14, fontweight='bold')
    ax.set_xlabel('Sepal Length (标准化)')
    ax.set_ylabel('Sepal Width (标准化)')
    ax.grid(True, alpha=0.3)

plt.suptitle('图3：K-means 决策边界 (基于前两个特征)', fontsize=15, fontweight='bold', y=1.01)
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig3_decision_boundary.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig3_decision_boundary.png")

# 4e. 性能指标对比图
fig, axes = plt.subplots(1, 3, figsize=(18, 5))

# 收集数据
k_list = []
init_list = []
sse_vals = []
sil_vals = []
db_vals = []
for k in K_VALUES:
    for idx in range(N_INIT):
        k_list.append(k)
        init_list.append(idx + 1)
        sse_vals.append(results[k][idx]['inertia'])
        sil_vals.append(results[k][idx]['silhouette'])
        db_vals.append(results[k][idx]['db_index'])

df_plot = pd.DataFrame({'K': k_list, 'Init': init_list, 'SSE': sse_vals, 'Silhouette': sil_vals, 'DB': db_vals})

# SSE (肘部法则)
for k in K_VALUES:
    subset = df_plot[df_plot['K'] == k]
    axes[0].scatter([k]*len(subset), subset['SSE'], s=60, alpha=0.7, zorder=5)
axes[0].plot(K_VALUES, [df_plot[df_plot['K']==k]['SSE'].mean() for k in K_VALUES], 'r-o', linewidth=2, markersize=10)
axes[0].set_title('SSE/Inertia (肘部法则)', fontsize=13, fontweight='bold')
axes[0].set_xlabel('K')
axes[0].set_ylabel('SSE')
axes[0].grid(True, alpha=0.3)

# Silhouette
for k in K_VALUES:
    subset = df_plot[df_plot['K'] == k]
    axes[1].scatter([k]*len(subset), subset['Silhouette'], s=60, alpha=0.7, zorder=5)
axes[1].plot(K_VALUES, [df_plot[df_plot['K']==k]['Silhouette'].mean() for k in K_VALUES], 'g-s', linewidth=2, markersize=10)
axes[1].set_title('Silhouette Score', fontsize=13, fontweight='bold')
axes[1].set_xlabel('K')
axes[1].set_ylabel('Silhouette')
axes[1].grid(True, alpha=0.3)

# Davies-Bouldin
for k in K_VALUES:
    subset = df_plot[df_plot['K'] == k]
    axes[2].scatter([k]*len(subset), subset['DB'], s=60, alpha=0.7, zorder=5)
axes[2].plot(K_VALUES, [df_plot[df_plot['K']==k]['DB'].mean() for k in K_VALUES], 'b-^', linewidth=2, markersize=10)
axes[2].set_title('Davies-Bouldin Index', fontsize=13, fontweight='bold')
axes[2].set_xlabel('K')
axes[2].set_ylabel('Davies-Bouldin')
axes[2].grid(True, alpha=0.3)

plt.suptitle('图4：不同K值下聚类性能指标对比', fontsize=14, fontweight='bold')
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig4_performance_metrics.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig4_performance_metrics.png")

# 4f. DBSCAN结果图
fig, axes = plt.subplots(2, 2, figsize=(14, 14))
for idx, (eps, min_samples) in enumerate(dbscan_params):
    ax = axes[idx // 2, idx % 2]
    db = DBSCAN(eps=eps, min_samples=min_samples)
    y_db = db.fit_predict(X_scaled)
    y_db_pca = y_db

    n_clusters = len(set(y_db)) - (1 if -1 in y_db else 0)
    n_noise = np.sum(y_db == -1)

    unique_labels = set(y_db)
    for label in unique_labels:
        if label == -1:
            ax.scatter(X_pca[y_db == -1, 0], X_pca[y_db == -1, 1], c='gray', s=15, marker='x', alpha=0.5, label='Noise')
        else:
            ax.scatter(X_pca[y_db == label, 0], X_pca[y_db == label, 1],
                      c=colors_k[label % len(colors_k)], s=20, alpha=0.7, edgecolors='k', linewidth=0.3,
                      label=f'Cluster {label+1}')

    ax.set_title(f'DBSCAN eps={eps}, min_samples={min_samples}\n{n_clusters} clusters, {n_noise} noise points',
                 fontsize=12, fontweight='bold')
    ax.legend(fontsize=8, loc='best')
    ax.grid(True, alpha=0.3)

plt.suptitle('图5：DBSCAN 密度聚类结果 (PCA)', fontsize=15, fontweight='bold', y=1.01)
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig5_dbscan_results.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig5_dbscan_results.png")

# 4g. DBSCAN最佳参数决策边界
best_db = DBSCAN(eps=0.7, min_samples=5)
y_best_db = best_db.fit_predict(X_2d)

fig, ax = plt.subplots(1, 1, figsize=(10, 8))

# 对于DBSCAN决策边界比较复杂，使用散点图
for label in set(y_best_db):
    mask = y_best_db == label
    if label == -1:
        ax.scatter(X_2d[mask, 0], X_2d[mask, 1], c='gray', s=25, marker='x', alpha=0.6, label='Noise')
    else:
        ax.scatter(X_2d[mask, 0], X_2d[mask, 1], c=colors_k[label % len(colors_k)],
                  s=25, alpha=0.8, edgecolors='k', linewidth=0.5, label=f'Cluster {label+1}')

ax.set_title('DBSCAN 聚类结果 (eps=0.7, min_samples=5)', fontsize=14, fontweight='bold')
ax.set_xlabel('Sepal Length (标准化)')
ax.set_ylabel('Sepal Width (标准化)')
ax.legend(fontsize=10)
ax.grid(True, alpha=0.3)

plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig6_dbscan_boundary.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig6_dbscan_boundary.png")

# ========== 5. 使用西瓜数据集额外实验 ==========
print("\n使用西瓜数据集4.0进行额外实验...")
watermelon = pd.read_csv("../../数据集/实验一数据集/数据集/4.0.csv", header=None, names=['density', 'sugar'])
X_wm = watermelon.values
X_wm_scaled = StandardScaler().fit_transform(X_wm)

fig, axes = plt.subplots(2, 2, figsize=(14, 14))
for idx, k in enumerate([2, 3, 4, 5]):
    ax = axes[idx // 2, idx % 2]

    best_sil = -1
    best_labels = None
    best_centroids = None
    for seed in [0, 42, 100]:
        km = KMeans(n_clusters=k, random_state=seed, n_init=1, init='random')
        labels = km.fit_predict(X_wm_scaled)
        try:
            sil = silhouette_score(X_wm_scaled, labels)
            if sil > best_sil:
                best_sil = sil
                best_labels = labels
                best_centroids = km.cluster_centers_
        except:
            pass

    if best_labels is None:
        continue

    # 决策边界
    x_min, x_max = X_wm_scaled[:, 0].min() - 0.5, X_wm_scaled[:, 0].max() + 0.5
    y_min, y_max = X_wm_scaled[:, 1].min() - 0.5, X_wm_scaled[:, 1].max() + 0.5
    xx, yy = np.meshgrid(np.linspace(x_min, x_max, 300), np.linspace(y_min, y_max, 300))

    km_plot = KMeans(n_clusters=k, random_state=42, n_init=1)
    km_plot.fit(X_wm_scaled)
    Z = km_plot.predict(np.c_[xx.ravel(), yy.ravel()]).reshape(xx.shape)

    colors_wm = ListedColormap(['#FFCCCC', '#CCEEEE', '#CCDDEE', '#FFFFCC', '#EECCFF'][:k])
    ax.contourf(xx, yy, Z, alpha=0.3, cmap=colors_wm)

    for c in range(k):
        mask = best_labels == c
        ax.scatter(X_wm_scaled[mask, 0], X_wm_scaled[mask, 1], c=colors_k[c], s=40, alpha=0.8, edgecolors='k', linewidth=0.8)
    ax.scatter(best_centroids[:, 0], best_centroids[:, 1], c='red', marker='X', s=200, edgecolors='black', linewidth=2, zorder=10)
    ax.set_title(f'西瓜数据集 K={k} (Sil={best_sil:.3f})', fontsize=13, fontweight='bold')
    ax.set_xlabel('密度 (标准化)')
    ax.set_ylabel('含糖率 (标准化)')
    ax.grid(True, alpha=0.3)

plt.suptitle('图7：西瓜数据集4.0 K-means聚类结果', fontsize=15, fontweight='bold', y=1.01)
plt.tight_layout()
plt.savefig(OUTPUT_DIR + 'fig7_watermelon_kmeans.png', dpi=120, bbox_inches='tight', facecolor='white', edgecolor='none')
plt.close()
print("  [ok] fig7_watermelon_kmeans.png")

# ========== 6. 输出分析摘要 ==========
print("\n" + "=" * 60)
print("结果分析")
print("=" * 60)

# 最佳K值分析
best_k = max(K_VALUES, key=lambda k: np.mean([r['silhouette'] for r in results[k]]))
print(f"\n1. 最佳K值分析:")
print(f"   - 根据Silhouette Score, 最佳K = {best_k}")
for k in K_VALUES:
    avg_sil = np.mean([r['silhouette'] for r in results[k]])
    avg_sse = np.mean([r['inertia'] for r in results[k]])
    print(f"   - K={k}: 平均Silhouette={avg_sil:.4f}, 平均SSE={avg_sse:.1f}")

print(f"\n2. 初始中心点影响:")
for k in K_VALUES:
    sse_values = [r['inertia'] for r in results[k]]
    sil_values = [r['silhouette'] for r in results[k]]
    print(f"   - K={k}: SSE标准差={np.std(sse_values):.2f}, Silhouette标准差={np.std(sil_values):.4f}")
    best_init = max(range(N_INIT), key=lambda i: results[k][i]['silhouette'])
    worst_init = min(range(N_INIT), key=lambda i: results[k][i]['silhouette'])
    print(f"     最佳初始组: {best_init+1} (Sil={results[k][best_init]['silhouette']:.4f}), 最差: {worst_init+1} (Sil={results[k][worst_init]['silhouette']:.4f})")

print(f"\n3. 肘部法则分析:")
for k in K_VALUES:
    avg_sse = np.mean([r['inertia'] for r in results[k]])
    print(f"   - K={k}: 平均SSE = {avg_sse:.1f}")

print(f"\n4. DBSCAN分析:")
print(f"   - DBSCAN不需要预设簇数，可以自动发现任意形状的簇")
print(f"   - eps参数控制邻域半径，min_samples控制核心点所需的最小邻居数")
print(f"   - 在Iris数据集上，合适的参数(eps≈0.7, min_samples=5)可以发现与真实类别接近的聚类结构")

print(f"\n输出图表已保存至: {OUTPUT_DIR}")
print("完成！")
