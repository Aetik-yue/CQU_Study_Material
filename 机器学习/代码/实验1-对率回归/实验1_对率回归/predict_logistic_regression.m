function [labels, probs] = predict_logistic_regression(model, X)
    X_bias = [ones(size(X, 1), 1), X];
    probs = sigmoid_stable(X_bias * model.weights);
    labels = double(probs >= 0.5);
end

function probs = sigmoid_stable(z)
    probs = zeros(size(z));

    non_negative = z >= 0;
    probs(non_negative) = 1 ./ (1 + exp(-z(non_negative)));

    exp_z = exp(z(~non_negative));
    probs(~non_negative) = exp_z ./ (1 + exp_z);
end
