import numpy as np
from scipy.optimize import fsolve
def solve_by_column_pivoting(A, b):
    # TODO: 请修改此函数的实现，使用列主元高斯消元法求解方程组
    return np.linalg.solve(A, b)
A = np.array([[1.1348, 3.8326, 1.1651, 3.4017], 
              [0.5301, 1.7875, 2.5330, 1.5435], 
              [3.4129, 4.9317, 8.7643, 1.3142],
              [1.2371, 4.9998, 10.6721, 0.0147]])
b = np.array([9.5342, 6.3941, 18.4231, 16.9237])
x = solve_by_column_pivoting(A, b)
print("计算结果：x =", x)