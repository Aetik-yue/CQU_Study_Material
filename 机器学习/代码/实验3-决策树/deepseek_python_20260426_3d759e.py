# 特征编码（全局编码器）
X_all, encoder = encode_features(df, discrete_cols, continuous_cols, fit_encoder=None)
y_all = df['好瓜'].values

# 训练多变量决策树
tree_model = LogisticRegressionTree(max_depth=3, min_samples_split=3)
tree_model.tree = tree_model.fit(X_all, y_all)

print("多变量决策树结构（每个节点为逻辑回归模型）：")
tree_model.print_tree(tree_model.tree)