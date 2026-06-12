import numpy as np

def yacobi(A, b, x0, tol=1e-6, max_iter=100):
    n = len(b)
    x = x0.copy()
    k = 0
    while k < max_iter:
        x_new = np.zeros_like(x)
        
        # 计算新的迭代值
        for i in range(n):
            # 计算除对角线外的总和
            sum_val = np.dot(A[i, :i], x[:i]) + np.dot(A[i, i+1:], x[i+1:])
            # 雅可比迭代公式
            x_new[i] = (b[i] - sum_val) / A[i, i]
        
        # 计算误差
        error = np.linalg.norm(x_new - x)
        
        # 输出当前迭代结果
        print(f"{k+1:8d} | {x_new[0]:.6f} | {x_new[1]:.6f} | {x_new[2]:.6f} | {error:.8f}")
        # 判断是否收敛
        if error < tol:
            x = x_new
            k += 1
            break
        x = x_new
        k += 1
    return x, k
A = np.array([[10, -1, -2],
              [-1, 10, -2],
              [-1, -1, 5]], dtype=np.float64)
b = np.array([7.2, 8.3, 4.2], dtype=np.float64) # 初始解向量
x0 = np.zeros_like(b) # 调用雅可比迭代法求解
x, k = yacobi(A, b, x0)
print("计算结果：x =", x)  # 输出最终结果
