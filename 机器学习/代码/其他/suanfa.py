def entropy(y):
    """计算信息熵"""
    counts = y.value_counts()
    probs = counts / len(y)
    return -sum(p * log2(p) for p in probs)

def gain_discrete(df, attr, label):
    """离散属性的信息增益"""
    total_ent = entropy(df[label])
    vals = df[attr].unique()
    weighted_ent = 0
    for v in vals:
        sub_df = df[df[attr] == v]
        weighted_ent += len(sub_df) / len(df) * entropy(sub_df[label])
    return total_ent - weighted_ent

def gain_continuous(df, attr, label):
    """连续属性的信息增益（返回最大增益及最佳分割点）"""
    total_ent = entropy(df[label])
    sorted_df = df.sort_values(attr)
    values = sorted_df[attr].values
    labels = sorted_df[label].values
    # 候选分割点：相邻两个值的中间点
    split_points = []
    for i in range(len(values)-1):
        if labels[i] != labels[i+1]:  # 仅在类别不同时考虑分割点
            split_points.append((values[i] + values[i+1]) / 2)
    best_gain = -1
    best_split = None
    for sp in split_points:
        left = df[df[attr] <= sp]
        right = df[df[attr] > sp]
        weighted_ent = (len(left)/len(df))*entropy(left[label]) + (len(right)/len(df))*entropy(right[label])
        gain = total_ent - weighted_ent
        if gain > best_gain:
            best_gain = gain
            best_split = sp
    return best_gain, best_split

def choose_best_attribute(df, attrs, label):
    """选择最佳划分属性（包括连续属性的分割点处理）"""
    best_gain = -1
    best_attr = None
    best_split = None
    for attr in attrs:
        if df[attr].dtype == 'object':  # 离散属性
            g = gain_discrete(df, attr, label)
            if g > best_gain:
                best_gain = g
                best_attr = attr
                best_split = None
        else:  # 连续属性
            g, split = gain_continuous(df, attr, label)
            if g > best_gain:
                best_gain = g
                best_attr = attr
                best_split = split
    return best_attr, best_split

def build_tree(df, attrs, label):
    """递归构建决策树"""
    # 如果所有样本类别相同，返回该类别
    if len(df[label].unique()) == 1:
        return df[label].iloc[0]
    # 如果没有可用属性，返回多数类别
    if len(attrs) == 0:
        return df[label].mode()[0]
    # 选择最佳属性
    best_attr, split_val = choose_best_attribute(df, attrs, label)
    tree = {best_attr: {}}
    if split_val is None:  # 离散属性
        for val in df[best_attr].unique():
            sub_df = df[df[best_attr] == val]
            sub_attrs = [a for a in attrs if a != best_attr]
            subtree = build_tree(sub_df, sub_attrs, label)
            tree[best_attr][val] = subtree
    else:  # 连续属性
        # 划分成两个分支：<= split_val 和 > split_val
        left_df = df[df[best_attr] <= split_val]
        right_df = df[df[best_attr] > split_val]
        sub_attrs = [a for a in attrs if a != best_attr]
        left_subtree = build_tree(left_df, sub_attrs, label)
        right_subtree = build_tree(right_df, sub_attrs, label)
        tree[best_attr][f'<={split_val:.3f}'] = left_subtree
        tree[best_attr][f'>{split_val:.3f}'] = right_subtree
    return tree

# 准备属性列表
attrs = list(df.columns[:-1])  # 去掉标签列
label = '好瓜'

# 构建决策树
tree = build_tree(df, attrs, label)
print("\n决策树结构（字典形式）：")
print(tree)