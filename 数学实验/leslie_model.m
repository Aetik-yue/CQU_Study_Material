%% Leslie 人口矩阵模型 —— 重庆市人口年龄结构预测
% 模型：x(k+1) = L * x(k)
% 使用5年年龄分组，考虑女性人口
clear; clc; close all;

fprintf('========== Leslie 矩阵模型 ==========\n\n');

%% 1. 建立年龄分组（5岁一组，0-4至95+，共20组）
age_groups = 0:5:95;
n_groups = length(age_groups);  % 20组

%% 2. 估算2020年重庆市女性各年龄段人口（基于七普数据）
% 2020年总人口3205.42万，女性约49.45%，即1585万
% 按年龄比例分配（参考重庆七普年龄结构及全国分布特征）
total_female_2020 = 3205.42 * 0.4945;

% 各年龄组女性比例（基于七普数据估算，单位：万人）
% 0-14岁: 509.84万*0.485(女性比) ≈ 247.27万 → 各5岁组约82万
% 15-59岁: 1994.54万*0.49 → 977万 → 9组，各组不等
% 60+: 701.04万*0.52(女性更多) → 365万 → 8组
female_props = [
    0.0255, 0.0255, 0.0250, ...          % 0-4, 5-9, 10-14
    0.0245, 0.0255, 0.0280, ...          % 15-19, 20-24, 25-29
    0.0320, 0.0340, 0.0350, ...          % 30-34, 35-39, 40-44
    0.0330, 0.0320, 0.0300, ...          % 45-49, 50-54, 55-59
    0.0280, 0.0240, 0.0200, ...          % 60-64, 65-69, 70-74
    0.0150, 0.0100, 0.0075, ...          % 75-79, 80-84, 85-89
    0.0040, 0.0015];                     % 90-94, 95+
x0_female = total_female_2020 * female_props';

fprintf('2020年女性总人口：%.2f 万人\n', total_female_2020);
fprintf('各年龄组女性人口（万人）：\n');
for i = 1:n_groups
    fprintf('  %d-%d岁: %.2f\n', age_groups(i), min(age_groups(i)+4, 100), x0_female(i));
end

%% 3. 构建 Leslie 矩阵 L
% L(i,1) = bi (生育率，仅育龄组15-49岁即第4-10组有值)
% L(i,i-1) = si (存活率，组i的人口存活到组i+1的比例)

L = zeros(n_groups, n_groups);

% 3.1 存活率（5年存活率，基于2020年重庆生命表估算）
% 年轻组存活率高，老年组逐渐降低
survival_5yr = [
    0.998, 0.998, 0.997, ...    % 0-4→5-9, 5-9→10-14, 10-14→15-19
    0.996, 0.995, 0.994, ...    % 15-19→20-24, 20-24→25-29, 25-29→30-34
    0.993, 0.991, 0.988, ...    % 30-34→35-39, 35-39→40-44, 40-44→45-49
    0.983, 0.975, 0.962, ...    % 45-49→50-54, 50-54→55-59, 55-59→60-64
    0.940, 0.905, 0.850, ...    % 60-64→65-69, 65-69→70-74, 70-74→75-79
    0.770, 0.660, 0.520, ...    % 75-79→80-84, 80-84→85-89, 85-89→90-94
    0.350];                     % 90-94→95+

for i = 2:n_groups
    L(i, i-1) = survival_5yr(i-1);
end

% 3.2 生育率（5年总和，仅15-49岁女性，使用重庆低生育水平）
% 重庆TFR约1.1-1.3，5年总和生育率需按年龄分布
% 生育率峰值在25-29岁
fertility_5yr = zeros(1, n_groups);
fertility_5yr(4:10) = [0.015, 0.085, 0.120, 0.080, 0.035, 0.010, 0.003];
% 第4-10组对应15-19至45-49岁

% 考虑新生婴儿女性比例（约0.485）
female_birth_ratio = 0.485;
L(1, :) = fertility_5yr * female_birth_ratio;

fprintf('\nLeslie矩阵构建完成（%d×%d）\n', n_groups, n_groups);
fprintf('总和生育率(TFR)估算：%.2f\n', sum(fertility_5yr) * 5 * female_birth_ratio * 2);

%% 4. 模型迭代预测（2020-2045，每5年一步，共5步）
n_steps = 5;  % 预测到2045年（5步×5年=25年，从2020起）
years_predict = 2020:5:2020+n_steps*5;

pop_female = zeros(n_groups, n_steps+1);
pop_female(:, 1) = x0_female;

for k = 1:n_steps
    pop_female(:, k+1) = L * pop_female(:, k);
end

% 转换为总人口（考虑性别比，男性约51.2%，女性约48.8%，近年趋于均衡）
% 使用女性人口×2.02近似总人口（因女性略少于男性）
pop_total = sum(pop_female) * 2.02;

%% 5. 输出结果
fprintf('\n========== Leslie 模型预测结果 ==========\n');
fprintf('年份    总人口(万)    0-14岁%%    15-59岁%%    60+岁%%    65+岁%%\n');
fprintf('--------------------------------------------------------\n');

for k = 1:n_steps+1
    young = sum(pop_female(1:3, k)) * 2.02;
    working = sum(pop_female(4:12, k)) * 2.02;
    old60 = sum(pop_female(13:end, k)) * 2.02;
    old65 = sum(pop_female(14:end, k)) * 2.02;
    total_k = sum(pop_female(:, k)) * 2.02;

    fprintf('%d    %.2f      %.2f      %.2f      %.2f      %.2f\n', ...
        years_predict(k), total_k, ...
        young/total_k*100, working/total_k*100, ...
        old60/total_k*100, old65/total_k*100);
end

%% 6. 人口金字塔图
figure('Position', [100, 100, 1200, 500]);

for k = 1:3:n_steps+1
    subplot(1, 3, ceil(k/3));
    pop_data = pop_female(:, k);
    barh(age_groups, pop_data, 'FaceColor', [0.3 0.6 0.9]);
    set(gca, 'YDir', 'reverse');
    xlabel('女性人口（万人）', 'FontSize', 12);
    ylabel('年龄组', 'FontSize', 12);
    title(sprintf('%d年女性人口金字塔', years_predict(k)), 'FontSize', 13);
    grid on;
end

saveas(gcf, '图片/人口模型/leslie_pyramid.png');

%% 7. 人口趋势对比图
figure('Position', [100, 100, 1000, 400]);

subplot(1,2,1);
plot(years_predict, pop_total, 'b-o', 'LineWidth', 2, 'MarkerSize', 8);
xlabel('年份', 'FontSize', 12);
ylabel('总人口（万人）', 'FontSize', 12);
title('Leslie模型：重庆市总人口预测', 'FontSize', 14);
grid on;

subplot(1,2,2);
young_ratio = zeros(1, n_steps+1);
working_ratio = zeros(1, n_steps+1);
old_ratio = zeros(1, n_steps+1);
old65_ratio = zeros(1, n_steps+1);

for k = 1:n_steps+1
    young_ratio(k) = sum(pop_female(1:3, k)) / sum(pop_female(:, k)) * 100;
    working_ratio(k) = sum(pop_female(4:12, k)) / sum(pop_female(:, k)) * 100;
    old_ratio(k) = sum(pop_female(13:end, k)) / sum(pop_female(:, k)) * 100;
    old65_ratio(k) = sum(pop_female(14:end, k)) / sum(pop_female(:, k)) * 100;
end

plot(years_predict, young_ratio, 'g-o', 'LineWidth', 2); hold on;
plot(years_predict, working_ratio, 'b-s', 'LineWidth', 2);
plot(years_predict, old_ratio, 'r-^', 'LineWidth', 2);
plot(years_predict, old65_ratio, 'm--d', 'LineWidth', 2);
xlabel('年份', 'FontSize', 12);
ylabel('占比 (%)', 'FontSize', 12);
title('Leslie模型：年龄结构变化趋势', 'FontSize', 14);
legend('0-14岁', '15-59岁', '60岁+', '65岁+', 'Location', 'best');
grid on;

saveas(gcf, '图片/人口模型/leslie_trend.png');
fprintf('\n图表已保存为 图片/人口模型/leslie_pyramid.png 和 leslie_trend.png\n');
