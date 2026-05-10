%% Malthus 人口指数增长模型 —— 重庆市人口预测
% 模型：dx/dt = r*x,  x(t) = x0 * exp(r*t)
clear; clc; close all;

%% 1. 导入数据
% 重庆市常住人口数据（万人），2014-2024年
year = (2014:2024)';
t_data = year - 2014;  % 以2014年为基准 t=0
pop_data = [3043.48; 3070.02; 3109.96; 3143.51; 3163.14; ...
            3124.32; 3205.42; 3212.43; 3213.34; 3191.43; 3190.47];

fprintf('========== Malthus 指数增长模型 ==========\n');
fprintf('年份    实际人口(万)    拟合值(万)    相对误差%%\n');
fprintf('------------------------------------------\n');

%% 2. 参数估计（线性最小二乘法）
% ln(x) = ln(x0) + r*t  →  y = a + b*t
y = log(pop_data);
X = [ones(length(t_data), 1), t_data];
b_hat = X \ y;          % 最小二乘解
x0_malthus = exp(b_hat(1));
r_malthus = b_hat(2);

fprintf('\n估计参数：x0 = %.4f 万人,  r = %.6f\n', x0_malthus, r_malthus);
fprintf('模型表达式：x(t) = %.4f * exp(%.6f * t)\n\n', x0_malthus, r_malthus);

%% 3. 模型计算与误差分析
pop_fit_malthus = x0_malthus * exp(r_malthus * t_data);
errors = abs(pop_fit_malthus - pop_data) ./ pop_data * 100;

for i = 1:length(year)
    fprintf('%d    %.2f        %.2f        %.4f\n', ...
        year(i), pop_data(i), pop_fit_malthus(i), errors(i));
end
fprintf('\n平均相对误差：%.4f%%\n', mean(errors));

%% 4. 未来20年预测
t_future = (0:30)';  % 预测到2044年（总共30年）
year_future = 2014 + t_future;
pop_predict_malthus = x0_malthus * exp(r_malthus * t_future);

fprintf('\n--- Malthus 模型未来预测 ---\n');
fprintf('年份    预测人口(万)\n');
fprintf('--------------------\n');
for i = 12:length(year_future)
    fprintf('%d    %.2f\n', year_future(i), pop_predict_malthus(i));
end

%% 5. 绘图
figure('Position', [100, 100, 900, 500]);

subplot(1,2,1);
plot(year, pop_data, 'bo', 'MarkerSize', 8, 'LineWidth', 1.5); hold on;
plot(year_future, pop_predict_malthus, 'r-', 'LineWidth', 2);
xlabel('年份', 'FontSize', 12);
ylabel('人口（万人）', 'FontSize', 12);
title('Malthus 模型：重庆市人口拟合与预测', 'FontSize', 14);
legend('实际数据', 'Malthus拟合/预测', 'Location', 'northwest');
grid on;

subplot(1,2,2);
bar(year, errors, 'FaceColor', [0.2 0.6 0.8]);
xlabel('年份', 'FontSize', 12);
ylabel('相对误差 (%)', 'FontSize', 12);
title('拟合相对误差', 'FontSize', 14);
grid on;

saveas(gcf, '图片/人口模型/malthus_result.png');
fprintf('\n图表已保存为 图片/人口模型/malthus_result.png\n');
