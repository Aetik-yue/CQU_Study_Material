% 线性规划模型 - 生产计划问题
% 使用 linprog 命令求解

% 清除工作区
clear;
clc;

%% 1. 定义目标函数系数（最大化问题需要取负）
% 原问题：max Z = 4*x1 + 3*x2 + 2*x3
% 转换为：min -Z = -4*x1 - 3*x2 - 2*x3
f = [-4; -3; -2];

%% 2. 定义不等式约束矩阵 A*x <= b
% 约束条件：
%   2*x1 + 3*x2 + 1*x3 <= 34  (材料)
%   3*x1 + 2*x2 + 1.5*x3 <= 36 (工时)
%   3*x1 + 2*x2 + 5*x3 <= 40  (人力)
A = [2,   3,   1;
     3,   2,   1.5;
     3,   2,   5];

b = [34; 36; 40];

%% 3. 定义等式约束（本问题无等式约束）
Aeq = [];
beq = [];

%% 4. 定义变量上下界
% x1, x2, x3 >= 0
lb = [0; 0; 0];
ub = [];  % 无上界

%% 5. 设置求解选项
options = optimoptions('linprog', ...
    'Algorithm', 'dual-simplex', ...
    'Display', 'iter');

%% 6. 调用 linprog 求解
[x, fval, exitflag, output] = linprog(f, A, b, Aeq, beq, lb, ub, options);

%% 7. 输出结果
fprintf('\n========================================\n');
fprintf('线性规划问题求解结果\n');
fprintf('========================================\n\n');

if exitflag == 1
    fprintf('✓ 求解成功！\n\n');
    fprintf('最优解：\n');
    fprintf('  x1 = %.4f\n', x(1));
    fprintf('  x2 = %.4f\n', x(2));
    fprintf('  x3 = %.4f\n\n', x(3));
    
    % 计算最大利润（注意要取负号）
    max_Z = -fval;
    fprintf('最大利润：Z = %.4f\n\n', max_Z);
    
    fprintf('求解信息：\n');
    fprintf('  迭代次数：%d\n', output.iterations);
    fprintf('  算法：%s\n', output.algorithm);
else
    fprintf('✗ 求解失败！\n');
    fprintf('退出标志：exitflag = %d\n', exitflag);
end

fprintf('========================================\n');
