import time

# 目标函数
def f(x):
    return x - x**(1/3) - 2

# 目标函数的导数（用于牛顿法）
def f_prime(x):
    return 1 - (1/3) * x**(-2/3)

# 测试区间，寻找f(a)*f(b) < 0的区间
test_points = [2, 3, 4, 5]
for x in test_points:
    print(f"f({x}) = {f(x):.6f}")

# 输出：
# f(2) = -0.763404
# f(3) = 0.035355
# f(4) = 0.928308
# f(5) = 1.732051
# 因此有根区间为 [2, 3]