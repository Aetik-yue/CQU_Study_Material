
#将f(x)=0变形为x=g(x),这里选择g(x)=x**(1/3)+2,并迭代求解。
import time
def fixed_point(x0, tol=1e-11, max_iter=1000):      ##定义不动点迭代法函数
    start_time = time.time()
    x = x0
    iter_count = 0
    while iter_count < max_iter: ##迭代次数不超过最大值
        x_new = x**(1/3) + 2
        error = abs(x_new - x)
        if error < tol:
            end_time = time.time()
            return x_new, error, iter_count, end_time - start_time  ##返回结果、误差、迭代次数和运行时间
        x = x_new
        iter_count += 1
    raise ValueError("不动点迭代未收敛")  ##若未收敛则报错

fp_result, fp_error, fp_iter, fp_time = fixed_point(3)  ##调用不动点迭代函数（初始值选3）
print("不动点迭代结果：", fp_result)
print("误差：", fp_error)
print("迭代次数：", fp_iter)
print("运行时间：", fp_time)