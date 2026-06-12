import numpy as np
# 画图
import matplotlib
from matplotlib import pyplot as plt
matplotlib.rcParams['font.family'] = 'Times New Roman'
matplotlib.rcParams['mathtext.default'] = 'regular'
# 最小二乘法拟合
def least_square(x, y, n):
    '''
    :param x: x坐标
    :param y: y坐标
    :param n: 最高次数
    :return: 返回拟合函数
    '''
    # TODO: 请修改本函数求解最小二乘法拟合函数
    coefficients = np.polyfit(x, y, n)
    # 创建一个多项式函数
    p = np.poly1d(coefficients)
    return p
times = np.arange(1, 25, 1) # 时间数据
temperature = np.array([
    58, 58, 58, 58, 57, 57, 57, 58, 60, 64, 67, 68,
    66, 66, 65, 64, 63, 63, 62, 61, 60, 60, 59, 58,
]) # 温度数据
p = least_square(times, temperature, 3) # 三次拟合
print(p)
# 画图
plt.figure(figsize=(8, 6), dpi=200)
plt.plot(times, temperature, 'ro', label='Original data')
plt.plot(times, p(times), label='Fitted line')
plt.xlabel('Time (h)')
plt.ylabel('Temperature (F)')
plt.legend()
plt.show()