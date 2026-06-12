import numpy as np
from scipy.optimize import fsolve
def solve_by_jacobi_iteration(A, b, max_iter=1000, tol=1e-6): # 可以根据需要增加参数
    # TODO: 请修改此函数的实现，使用Jacobi迭代法求解方程组
    return np.linalg.solve(A, b)
A = np.array([[10, -1, -2],
              [-1, 10, -2],
              [-1, -1, 5]])
b = np.array([7.2, 8.3, 4.2])
x = solve_by_jacobi_iteration(A, b) # 或者 solve_by_gauss_seidel_iteration(A, b)
print("计算结果：x =", x)