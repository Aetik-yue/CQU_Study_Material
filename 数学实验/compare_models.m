%% 综合对比脚本：三种人口模型预测重庆市人口
% 包含 Malthus 模型、Logistic 模型和 Leslie 模型
clear; clc; close all;

%% ========== 数据准备 ==========
year_data = (2014:2024)';
t_data = year_data - 2014;
pop_data = [3043.48; 3070.02; 3109.96; 3143.51; 3163.14; ...
            3124.32; 3205.42; 3212.43; 3213.34; 3191.43; 3190.47];

%% ========== 1. Malthus 模型 ==========
y_log = log(pop_data);
X_m = [ones(length(t_data), 1), t_data];
b_m = X_m \ y_log;
x0_m = exp(b_m(1));
r_m = b_m(2);

t_pred = (0:30)';
year_pred = 2014 + t_pred;
pop_malthus = x0_m * exp(r_m * t_pred);
pop_malthus_fit = x0_m * exp(r_m * t_data);
err_malthus = mean(abs(pop_malthus_fit - pop_data) ./ pop_data * 100);

%% ========== 2. Logistic 模型 ==========
logistic_fun = @(b, t) b(2) ./ (1 + (b(2)/b(1) - 1) .* exp(-b(3) .* t));
b_init = [pop_data(1), max(pop_data)*1.1, 0.005];
options_opt = optimset('Display', 'off', 'MaxFunEvals', 10000, 'MaxIter', 10000);

try
    b_hat = lsqcurvefit(logistic_fun, b_init, t_data', pop_data', ...
        [0, 3000, 0], [5000, 5000, 0.5], options_opt);
catch
    obj_f = @(b) sum((logistic_fun(b, t_data') - pop_data').^2);
    b_hat = fminsearch(obj_f, b_init, options_opt);
end

pop_logistic = logistic_fun(b_hat, t_pred');
pop_logistic_fit = logistic_fun(b_hat, t_data');
err_logistic = mean(abs(pop_logistic_fit - pop_data) ./ pop_data * 100);

%% ========== 3. 结果汇总输出 ==========
fprintf('==================== 三种模型参数估计 ====================\n\n');

fprintf('【Malthus 指数增长模型】\n');
fprintf('  x0 = %.4f,  r = %.6f\n', x0_m, r_m);
fprintf('  模型：x(t) = %.4f * exp(%.6f * t)\n', x0_m, r_m);
fprintf('  平均拟合误差：%.4f%%\n\n', err_malthus);

fprintf('【Logistic 阻滞增长模型】\n');
fprintf('  x0 = %.4f,  xm = %.4f,  r = %.6f\n', b_hat(1), b_hat(2), b_hat(3));
fprintf('  模型：x(t) = %.4f / (1 + (%.4f/%.4f - 1) * exp(-%.6f * t))\n', ...
    b_hat(2), b_hat(2), b_hat(1), b_hat(3));
fprintf('  平均拟合误差：%.4f%%\n\n', err_logistic);

fprintf('【Leslie 矩阵模型】\n');
fprintf('  详见 leslie_model.m 运行结果\n\n');

fprintf('==================== 未来人口预测对比 ====================\n');
fprintf('年份      Malthus(万)    Logistic(万)\n');
fprintf('--------------------------------------\n');
for i = 1:6
    idx = 11 + i;  % 2025-2030
    fprintf('%d      %.2f        %.2f\n', year_pred(idx), pop_malthus(idx), pop_logistic(idx));
end
for i = 7:11
    idx = 11 + i;  % 2031-2035
    fprintf('%d      %.2f        %.2f\n', year_pred(idx), pop_malthus(idx), pop_logistic(idx));
end
for i = 12:length(t_pred)-11
    idx = 22 + i;  % 2036-2044
    if idx <= length(year_pred)
        fprintf('%d      %.2f        %.2f\n', year_pred(idx), pop_malthus(idx), pop_logistic(idx));
    end
end

%% ========== 4. 综合对比绘图 ==========
figure('Position', [50, 50, 1400, 500]);

% 图1: Malthus vs Logistic 拟合与预测
subplot(1,3,1);
plot(year_data, pop_data, 'ko', 'MarkerSize', 8, 'LineWidth', 1.5); hold on;
h1 = plot(year_pred, pop_malthus, 'r-', 'LineWidth', 2);
h2 = plot(year_pred, pop_logistic, 'b-', 'LineWidth', 2);
yline(b_hat(2), 'b--', 'LineWidth', 1);
xlabel('年份'); ylabel('人口（万人）');
title('Malthus 与 Logistic 模型对比');
legend([h1, h2], {'Malthus模型', 'Logistic模型'}, 'Location', 'northwest');
grid on;

% 图2: 拟合误差对比
subplot(1,3,2);
b1 = bar(year_data, [abs(pop_malthus_fit-pop_data)./pop_data*100, ...
                     abs(pop_logistic_fit-pop_data)./pop_data*100]);
b1(1).FaceColor = [0.8 0.3 0.3];
b1(2).FaceColor = [0.3 0.5 0.8];
xlabel('年份'); ylabel('相对误差 (%)');
title('拟合误差对比');
legend('Malthus误差', 'Logistic误差', 'Location', 'northwest');
grid on;

% 图3: 增长率对比
subplot(1,3,3);
r_malthus_curve = r_m * ones(size(t_pred));
r_logistic_curve = b_hat(3) * (1 - pop_logistic / b_hat(2));
plot(year_pred, r_malthus_curve*100, 'r--', 'LineWidth', 2); hold on;
plot(year_pred, r_logistic_curve*100, 'b-', 'LineWidth', 2);
xlabel('年份'); ylabel('增长率 (%)');
title('人口增长率对比');
legend('Malthus(恒定)', 'Logistic(递减)', 'Location', 'northeast');
grid on;

saveas(gcf, '图片/人口模型/model_comparison.png');
fprintf('\n综合对比图已保存为 图片/人口模型/model_comparison.png\n');
