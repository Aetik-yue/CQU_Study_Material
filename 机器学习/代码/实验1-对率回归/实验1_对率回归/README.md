# 对率回归算法复现

这个目录给出一个不用现成分类库、完全手写训练过程的 MATLAB 版本对率回归实验。

## 文件说明

- `run_experiment.m`：主脚本，负责读取数据、训练模型、评估结果、保存预测和绘图。
- `train_logistic_regression_newton.m`：手写 Newton 法训练器，包含 sigmoid、负对数似然、梯度、Hessian 和回溯线搜索。
- `predict_logistic_regression.m`：根据训练好的参数输出预测概率和预测标签。
- `outputs/`：运行后自动生成，保存训练记录、预测结果和决策边界图片。

## 运行方式

在 MATLAB 当前目录切到本文件夹后运行：

```matlab
run_experiment
```

## 实现约束

- 没有调用 `fitclinear`、`glmfit`、`mnrfit` 等现成模型训练函数。
- 模型参数由代码显式迭代求解。
- 数据读取和结果写出只使用 MATLAB 基础 I/O 接口。
