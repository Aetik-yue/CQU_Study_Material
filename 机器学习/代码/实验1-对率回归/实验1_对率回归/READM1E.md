# Python 复现对率回归

这份代码使用 Python 手写实现对率回归，训练过程采用 Newton 法，不调用 `sklearn`、`statsmodels` 等现成模型训练库。

## 文件说明

- `logistic_regression_from_scratch.py`：主脚本，包含数据读取、训练、预测、结果保存和绘图。
- `outputs/`：运行后自动生成，保存训练历史、预测结果和决策边界图。

## 运行方式

在 PowerShell 里执行：

```powershell
py .\logistic_regression_python\logistic_regression_from_scratch.py
```

如果你想显式指定数据集路径，也可以这样运行：

```powershell
py .\logistic_regression_python\logistic_regression_from_scratch.py `
  --train-path "C:\Users\yanha\Desktop\机器学习\实验\数据集\数据集\3.0a.csv" `
  --predict-path "C:\Users\yanha\Desktop\机器学习\实验\数据集\数据集\4.0.csv"
```

## 实现要点

- 手写稳定版 `sigmoid`
- 手写负对数似然损失
- 手写梯度与 Hessian
- 使用 Newton 法更新参数
- 使用回溯线搜索保证每一步下降更稳
- 使用 `numpy` 只做基础矩阵运算，不调用任何现成分类器
