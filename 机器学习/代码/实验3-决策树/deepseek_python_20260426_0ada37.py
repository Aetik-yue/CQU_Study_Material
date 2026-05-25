class LogisticRegressionTree:
    def __init__(self, max_depth=5, min_samples_split=2):
        self.max_depth = max_depth
        self.min_samples_split = min_samples_split
        self.tree = None
        self.encoder = None  # 存储全局编码器

    def fit(self, X, y, depth=0):
        # 停止条件
        if len(np.unique(y)) == 1 or depth >= self.max_depth or len(y) < self.min_samples_split:
            # 叶节点：返回多数类别
            return np.bincount(y).argmax()
        # 训练逻辑回归模型
        lr = LogisticRegression(max_iter=1000)
        lr.fit(X, y)
        # 预测划分
        y_pred = lr.predict(X)
        # 如果划分无效（所有样本分到同一侧），则停止并返回多数类
        if len(np.unique(y_pred)) == 1:
            return np.bincount(y).argmax()
        # 划分左右子集
        left_idx = np.where(y_pred == 0)[0]
        right_idx = np.where(y_pred == 1)[0]
        # 递归构建子树
        left_subtree = self.fit(X[left_idx], y[left_idx], depth+1)
        right_subtree = self.fit(X[right_idx], y[right_idx], depth+1)
        # 返回节点
        return {'lr': lr, 'left': left_subtree, 'right': right_subtree}

    def predict_one(self, x, node):
        if not isinstance(node, dict):
            return node
        pred = node['lr'].predict(x.reshape(1, -1))[0]
        if pred == 0:
            return self.predict_one(x, node['left'])
        else:
            return self.predict_one(x, node['right'])

    def predict(self, X):
        return np.array([self.predict_one(x, self.tree) for x in X])

    def print_tree(self, node, indent=''):
        if not isinstance(node, dict):
            print(indent + '--> 类别：', '是' if node == 1 else '否')
            return
        print(indent + '逻辑回归模型：')
        # 打印模型系数（简化显示）
        coef = node['lr'].coef_[0]
        intercept = node['lr'].intercept_[0]
        print(indent + f'  决策值 = {intercept:.3f}', end='')
        for i, c in enumerate(coef):
            print(f' + {c:.3f} * x{i}', end='')
        print()
        print(indent + '  左子树（预测为0）：')
        self.print_tree(node['left'], indent + '    ')
        print(indent + '  右子树（预测为1）：')
        self.print_tree(node['right'], indent + '    ')