clear;
clc;

script_dir = fileparts(mfilename('fullpath'));
experiment_dir = fileparts(script_dir);
data_dir = fullfile(experiment_dir, '数据集', '数据集');
output_dir = fullfile(script_dir, 'outputs');

if ~exist(output_dir, 'dir')
    mkdir(output_dir);
end

train_path = fullfile(data_dir, '3.0a.csv');
predict_path = fullfile(data_dir, '4.0.csv');

train_data = readmatrix(train_path);
predict_data = readmatrix(predict_path);

X_train = train_data(:, 2:3);
y_train = train_data(:, 4);
X_predict = predict_data(:, 1:2);

options.max_iter = 50;
options.tol = 1e-8;
options.regularization = 1e-6;
options.line_search = true;

model = train_logistic_regression_newton(X_train, y_train, options);
[train_labels, train_probs] = predict_logistic_regression(model, X_train);
[predict_labels, predict_probs] = predict_logistic_regression(model, X_predict);

train_accuracy = mean(train_labels == y_train);

fprintf('===== 对率回归实验结果 =====\n');
fprintf('迭代次数: %d\n', model.iterations);
fprintf('偏置项 b: %.6f\n', model.weights(1));
fprintf('权重 w1: %.6f\n', model.weights(2));
fprintf('权重 w2: %.6f\n', model.weights(3));
fprintf('最终损失: %.10f\n', model.loss_history(end));
fprintf('训练集准确率: %.2f%%\n', train_accuracy * 100);

history_table = table((1:model.iterations)', model.loss_history, ...
    model.grad_norm_history, model.step_norm_history, ...
    'VariableNames', {'iteration', 'loss', 'grad_norm', 'step_norm'});
writetable(history_table, fullfile(output_dir, 'training_history.csv'));

train_result_table = table(train_data(:, 1), X_train(:, 1), X_train(:, 2), ...
    y_train, train_probs, train_labels, ...
    'VariableNames', {'编号', '密度', '含糖率', '真实标签', '预测概率', '预测标签'});
writetable(train_result_table, fullfile(output_dir, 'train_predictions.csv'));

predict_result_table = table((1:size(X_predict, 1))', X_predict(:, 1), ...
    X_predict(:, 2), predict_probs, predict_labels, ...
    'VariableNames', {'编号', '密度', '含糖率', '预测概率', '预测标签'});
writetable(predict_result_table, fullfile(output_dir, 'predict_4_0.csv'));

plot_decision_boundary(X_train, y_train, X_predict, model, ...
    fullfile(output_dir, 'decision_boundary.png'));

fprintf('结果文件已保存到: %s\n', output_dir);

function plot_decision_boundary(X_train, y_train, X_predict, model, save_path)
    fig = figure('Visible', 'off');
    hold on;
    grid on;
    box on;

    positive_mask = (y_train == 1);
    negative_mask = ~positive_mask;

    scatter(X_train(positive_mask, 1), X_train(positive_mask, 2), ...
        70, [0.85, 0.20, 0.20], 'filled', 'DisplayName', '好瓜(训练集)');
    scatter(X_train(negative_mask, 1), X_train(negative_mask, 2), ...
        70, [0.20, 0.35, 0.85], 'filled', 'DisplayName', '坏瓜(训练集)');
    scatter(X_predict(:, 1), X_predict(:, 2), 50, [0.15, 0.15, 0.15], ...
        'o', 'LineWidth', 1.2, 'DisplayName', '4.0待预测样本');

    all_x = [X_train(:, 1); X_predict(:, 1)];
    x_span = linspace(min(all_x) - 0.02, max(all_x) + 0.02, 200);

    if abs(model.weights(3)) > 1e-12
        y_span = -(model.weights(1) + model.weights(2) * x_span) / model.weights(3);
        plot(x_span, y_span, 'k-', 'LineWidth', 1.8, 'DisplayName', '决策边界');
    end

    xlabel('密度');
    ylabel('含糖率');
    title('手写对率回归的分类结果与决策边界');
    legend('Location', 'best');

    exportgraphics(fig, save_path, 'Resolution', 150);
    close(fig);
end
