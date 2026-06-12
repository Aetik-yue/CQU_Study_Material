import numpy as np

def gaosi(A, b):   ##编写高斯消元法
    n = len(b)      # 获取矩阵维度 
    Ab = np.hstack((A, b.reshape(n, 1)))# 创建增广矩阵 [A|b]
    for k in range(n-1):    
        max_row = np.argmax(np.abs(Ab[k:n, k])) + k   # 寻找主元（最大元素）以减少数值误差
        Ab[[k, max_row]] = Ab[[max_row, k]]     # 交换行
        for i in range(k+1, n):
            factor = Ab[i, k] / Ab[k, k]
            Ab[i, k:n+1] = Ab[i, k:n+1] - factor * Ab[k, k:n+1]
    # 回代过程求解x
    x = np.zeros(n)
    for i in range(n-1, -1, -1):
        x[i] = (Ab[i, n] - np.dot(Ab[i, i+1:n], x[i+1:n])) / Ab[i, i]
    return x
# 定义系数矩阵A
A = np.array([
    [1.1348, 3.8326, 1.1651, 3.4017],
    [0.5301, 1.7875, 2.5330, 1.5435],
    [3.4129, 4.9317, 8.7643, 1.3142],
    [1.2371, 4.9998, 10.6721, 0.0147]
], dtype=np.float64)

# 定义常数项向量b
b = np.array([9.5342, 6.3941, 18.4231, 16.9237], dtype=np.float64)

# 使用自定义的高斯消元法求解
x = gaosi(A, b)
print("计算结果：x =", x)
# 输出结果
