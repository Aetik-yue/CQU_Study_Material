function model = train_logistic_regression_newton(X, y, options)
    arguments
        X double
        y double
        options.max_iter (1,1) double = 50
        options.tol (1,1) double = 1e-8
        options.regularization (1,1) double = 1e-6
        options.line_search (1,1) logical = true
    end

    [sample_count, feature_count] = size(X);
    X_bias = [ones(sample_count, 1), X];
    weights = zeros(feature_count + 1, 1);

    loss_history = zeros(options.max_iter, 1);
    grad_norm_history = zeros(options.max_iter, 1);
    step_norm_history = zeros(options.max_iter, 1);

    lambda = options.regularization;
    last_iteration = options.max_iter;

    for iter = 1:options.max_iter
        logits = X_bias * weights;
        probs = sigmoid_stable(logits);

        loss = negative_log_likelihood(X_bias, y, weights, lambda);
        grad = X_bias' * (probs - y) + [0; lambda * weights(2:end)];
        curvature = probs .* (1 - probs);
        hessian = X_bias' * (X_bias .* curvature) + diag([0; lambda * ones(feature_count, 1)]);

        if rcond(hessian) < 1e-12
            hessian = hessian + 1e-6 * eye(feature_count + 1);
        end

        step = hessian \ grad;
        step_scale = 1.0;

        if options.line_search
            current_loss = loss;
            while step_scale > 1e-4
                candidate = weights - step_scale * step;
                candidate_loss = negative_log_likelihood(X_bias, y, candidate, lambda);
                if candidate_loss <= current_loss
                    break;
                end
                step_scale = step_scale * 0.5;
            end
        end

        weights = weights - step_scale * step;

        loss_history(iter) = negative_log_likelihood(X_bias, y, weights, lambda);
        grad_norm_history(iter) = norm(grad);
        step_norm_history(iter) = norm(step_scale * step);

        if grad_norm_history(iter) < options.tol || step_norm_history(iter) < options.tol
            last_iteration = iter;
            break;
        end
    end

    model.weights = weights;
    model.iterations = last_iteration;
    model.loss_history = loss_history(1:last_iteration);
    model.grad_norm_history = grad_norm_history(1:last_iteration);
    model.step_norm_history = step_norm_history(1:last_iteration);
end

function probs = sigmoid_stable(z)
    probs = zeros(size(z));

    non_negative = z >= 0;
    probs(non_negative) = 1 ./ (1 + exp(-z(non_negative)));

    exp_z = exp(z(~non_negative));
    probs(~non_negative) = exp_z ./ (1 + exp_z);
end

function loss = negative_log_likelihood(X_bias, y, weights, lambda)
    probs = sigmoid_stable(X_bias * weights);
    probs = min(max(probs, 1e-12), 1 - 1e-12);
    loss = -sum(y .* log(probs) + (1 - y) .* log(1 - probs)) ...
        + 0.5 * lambda * sum(weights(2:end) .^ 2);
end
