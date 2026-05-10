%% Logistic 阻滞增长模型 —— 重庆市人口预测
% 模型：dx/dt = r*(1 - x/xm)*x
% 解析解：x(t) = xm / (1 + (xm/x0 - 1) * exp(-r*t))
clear; clc; close all;

%% 1. 导入数据
year = (2014:2024)';
t_data = year - 2014;
pop_data = [3043.48; 3070.02; 3109.96; 3143.51; 3163.14; ...
            3124.32; 3205.42; 3212.43; 3213.34; 3191.43; 3190.47];

fprintf('========== Logistic 阻滞增长模型 ==========\n');

%% 2. 参数估计（非线性最小二乘）
% Logistic 函数形式
logistic_func = @(beta, t) beta(2) ./ (1 + (beta(2)/beta(1) - 1) .* exp(-beta(3) .* t));
% beta(1) = x0, beta(2) = xm, beta(3) = r

% 初始猜测
x0_guess = pop_data(1);
xm_guess = max(pop_data) * 1.1;   % 环境容量略大于最大值
r_guess = 0.005;
beta_init = [x0_guess, xm_guess, r_guess];

% 使用 lsqcurvefit 进行非线性最小二乘拟合
options = optimset('Display', 'off', 'MaxFunEvals', 10000, 'MaxIter', 10000);
try
    beta_hat = lsqcurvefit(logistic_func, beta_init, t_data', pop_data', ...
        [0, 3000, 0], [5000, 5000, 0.5], options);
catch
    % 如果优化工具箱不可用，使用 fminsearch
    obj_fun = @(beta) sum((logistic_func(beta, t_data') - pop_data').^2);
    beta_hat = fminsearch(obj_fun, beta_init, options);
end

x0_logistic = beta_hat(1);
xm_logistic = beta_hat(2);
r_logistic = beta_hat(3);

fprintf('\n估计参数：\n');
fprintf('  初始人口 x0 = %.4f 万人\n', x0_logistic);
fprintf('  最大容量 xm = %.4f 万人\n', xm_logistic);
fprintf('  固有增长率 r = %.6f\n', r_logistic);
fprintf('  模型表达式：x(t) = %.4f / (1 + (%.4f/%.4f - 1) * exp(-%.6f * t))\n\n', ...
    xm_logistic, xm_logistic, x0_logistic, r_logistic);

%% 3. 模型计算与误差分析
pop_fit_logistic = logistic_func(beta_hat, t_data');

fprintf('年份    实际人口(万)    拟合值(万)    相对误差%%\n');
fprintf('------------------------------------------\n');
errors_log = abs(pop_fit_logistic - pop_data) ./ pop_data * 100;
for i = 1:length(year)
    fprintf('%d    %.2f        %.2f        %.4f\n', ...
        year(i), pop_data(i), pop_fit_logistic(i), errors_log(i));
end
fprintf('\n平均相对误差：%.4f%%\n', mean(errors_log));

%% 4. 未来预测（至2044年）
t_future = (0:30)';
year_future = 2014 + t_future;
pop_predict_logistic = logistic_func(beta_hat, t_future');

fprintf('\n--- Logistic 模型未来预测 ---\n');
fprintf('年份    预测人口(万)\n');
fprintf('--------------------\n');
for i = 12:length(year_future)
    fprintf('%d    %.2f\n', year_future(i), pop_predict_logistic(i));
end

%% 5. 绘图
figure('Position', [100, 100, 1000, 400]);

subplot(1,3,1);
plot(year, pop_data, 'bo', 'MarkerSize', 8, 'LineWidth', 1.5); hold on;
plot(year_future, pop_predict_logistic, 'g-', 'LineWidth', 2);
yline(xm_logistic, 'k--', 'LineWidth', 1);
xlabel('年份', 'FontSize', 12);
ylabel('人口（万人）', 'FontSize', 12);
title('Logistic 模型：重庆市人口拟合与预测', 'FontSize', 13);
legend('实际数据', 'Logistic拟合/预测', '环境容量 x_m', 'Location', 'northwest');
grid on;

subplot(1,3,2);
bar(year, errors_log, 'FaceColor', [0.2 0.8 0.4]);
xlabel('年份', 'FontSize', 12);
ylabel('相对误差 (%)', 'FontSize', 12);
title('拟合相对误差', 'FontSize', 13);
grid on;

% 增长率曲线
subplot(1,3,3);
r_t = r_logistic * (1 - pop_predict_logistic / xm_logistic);
plot(year_future, r_t * 100, 'm-', 'LineWidth', 2);
xlabel('年份', 'FontSize', 12);
ylabel('增长率 (%)', 'FontSize', 12);
title('增长率随时间变化', 'FontSize', 13);
grid on;

saveas(gcf, '图片/人口模型/logistic_result.png');
fprintf('\n图表已保存为 图片/人口模型/logistic_result.png\n');
