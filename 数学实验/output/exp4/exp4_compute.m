function exp4_compute()
close all; clc;
baseDir = fileparts(mfilename('fullpath'));
figDir = fullfile(baseDir, 'figures');
if ~exist(figDir, 'dir')
    mkdir(figDir);
end
resultFile = fullfile(baseDir, 'results.txt');
if exist(resultFile, 'file')
    delete(resultFile);
end
diary(resultFile);
fprintf('Experiment 4 numerical results\n');

%% Basic experiment 1
x = linspace(0, 5, 1001);
trapValue = trapz(x, x .* exp(-x / 3));
quadValue = integral(@(x) x .* exp(-x / 3), 0, 5, ...
    'AbsTol', 1e-12, 'RelTol', 1e-12);
exactValue = 9 - 24 * exp(-5 / 3);
fprintf('\n[Basic 1]\n');
fprintf('trapz = %.12f, integral = %.12f, exact = %.12f\n', ...
    trapValue, quadValue, exactValue);
fprintf('trapz error = %.3e, integral error = %.3e\n', ...
    abs(trapValue - exactValue), abs(quadValue - exactValue));
f = figure('Visible', 'off');
plot(x, x .* exp(-x / 3), 'LineWidth', 1.2); grid on;
xlabel('x'); ylabel('x exp(-x/3)');
title('Integrand of Basic Experiment 1');
exportgraphics(f, fullfile(figDir, 'basic1_integrand.png'), 'Resolution', 180);

%% Basic experiment 2
t = linspace(0, 1, 200);
yAnalytic = 3 * exp(t) - 2 * t - 2;
[tn2, yn2] = ode45(@(t, y) [y(2); -y(1) * cos(t)], [0, 20], [1; 0], ...
    odeset('RelTol', 1e-10, 'AbsTol', 1e-12));
fprintf('\n[Basic 2]\n');
fprintf('Equation 1: y(x)=3 exp(x)-2x-2; y(1)=%.12f\n', yAnalytic(end));
fprintf('Equation 2: ode45 y(20)=%.12f, y''(20)=%.12f\n', yn2(end,1), yn2(end,2));
f = figure('Visible', 'off');
plot(t, yAnalytic, 'LineWidth', 1.2); grid on;
xlabel('x'); ylabel('y');
title('Analytical solution y = 3e^x - 2x - 2');
exportgraphics(f, fullfile(figDir, 'basic2_analytic.png'), 'Resolution', 180);
f = figure('Visible', 'off');
plot(tn2, yn2(:,1), 'LineWidth', 1.2); grid on;
xlabel('x'); ylabel('y');
title('Numerical solution of y'''' + y cos(x) = 0');
exportgraphics(f, fullfile(figDir, 'basic2_numeric.png'), 'Resolution', 180);

%% Basic experiment 5, Apollo problem
apolloT = 6.192169331319639;
apolloY0 = [1.2; 0; 0; -1.04935751];
opts = odeset('RelTol', 1e-9, 'AbsTol', 1e-11);
solverList = {'ode23', 'ode45', 'ode113', 'ode15s'};
solverLabels = solverList;
if exist('ode87', 'file') == 2
    solverList = [{'ode87'}, solverList];
    solverLabels = [{'ode87'}, solverLabels];
else
    solverList = [{'ode78'}, solverList];
    solverLabels = [{'ode78 (used for ode87)'}, solverLabels];
end
fprintf('\n[Basic 5 - Apollo]\n');
fprintf('%-22s %10s %12s %14s\n', 'solver', 'steps', 'time_sec', 'period_error');
apolloSummary = cell(numel(solverList), 4);
f1 = figure('Visible', 'off'); hold on; grid on;
f2 = figure('Visible', 'off'); hold on; grid on;
for k = 1:numel(solverList)
    solver = str2func(solverList{k});
    tic;
    [ts, ys] = solver(@apolloRhs, [0 apolloT], apolloY0, opts);
    elapsed = toc;
    err = norm(ys(end, :)' - apolloY0, inf);
    apolloSummary(k, :) = {solverLabels{k}, length(ts) - 1, elapsed, err};
    fprintf('%-22s %10d %12.4f %14.3e\n', solverLabels{k}, length(ts) - 1, elapsed, err);
    figure(f1); plot(ys(:,1), ys(:,3), 'DisplayName', solverLabels{k}, 'LineWidth', 1.0);
    figure(f2); semilogy(ts(1:end-1), diff(ts), 'DisplayName', solverLabels{k}, 'LineWidth', 1.0);
end
figure(f1); axis equal; xlabel('x'); ylabel('y'); title('Apollo orbit phase curve'); legend('Location', 'best');
exportgraphics(f1, fullfile(figDir, 'basic5_apollo_orbit.png'), 'Resolution', 180);
figure(f2); xlabel('t'); ylabel('step size'); title('Apollo solver step sizes'); legend('Location', 'best');
exportgraphics(f2, fullfile(figDir, 'basic5_apollo_steps.png'), 'Resolution', 180);
writecell([{'solver','steps','time_sec','period_error'}; apolloSummary], ...
    fullfile(baseDir, 'apollo_summary.csv'));

%% Basic experiment 5, double-pendulum-like system
u0 = [deg2rad(45); deg2rad(30); 0; 0];
[tp, up] = ode45(@pendulumRhs, [0 10], u0, opts);
fprintf('\n[Basic 5 - coupled pendulum]\n');
fprintf('u(10) = [%.10f %.10f %.10f %.10f]\n', up(end, :));
f = figure('Visible', 'off');
plot(tp, up, 'LineWidth', 1.0); grid on;
xlabel('t'); ylabel('state'); title('Coupled pendulum states');
legend('u_1','u_2','u_3','u_4','Location','best');
exportgraphics(f, fullfile(figDir, 'basic5_pendulum_states.png'), 'Resolution', 180);
f = figure('Visible', 'off');
plot(up(:,1), up(:,2), 'LineWidth', 1.0); grid on;
xlabel('u_1'); ylabel('u_2'); title('Coupled pendulum phase curve');
exportgraphics(f, fullfile(figDir, 'basic5_pendulum_phase.png'), 'Resolution', 180);

%% Basic experiment 5, implicit ODE
y0 = [1; 1; 2; 2];
v1 = y0(2); x1 = y0(1); x2 = y0(3); v2 = y0(4);
a2 = fzero(@(a2) ...
    2 * (exp(-x2^2) - v1 * a2 * sin(x1 * x2)) / (5 * v2 * cos(x1^2)) + ...
    a2 * v1 * sin(x1^2) + cos(a2 * x2), 1);
a1 = (exp(-x2^2) - v1 * a2 * sin(x1 * x2)) / (5 * v2 * cos(x1^2));
y0c = y0;
yp0c = [v1; a1; v2; a2];
tImplicitEnd = 0.25;
[ti, yi] = ode15i(@implicitResidual, [0 tImplicitEnd], y0c, yp0c, ...
    odeset('RelTol', 1e-7, 'AbsTol', 1e-9));
fprintf('\n[Basic 5 - implicit ODE]\n');
fprintf('consistent yp(0) = [%.10f %.10f %.10f %.10f]\n', yp0c);
fprintf('state at t=%.6f = [%.10f %.10f %.10f %.10f]\n', ti(end), yi(end, :));
f = figure('Visible', 'off');
plot(ti, yi(:,[1 3]), 'LineWidth', 1.0); grid on;
xlabel('t'); ylabel('x'); title('Implicit ODE solution');
legend('x_1','x_2','Location','best');
exportgraphics(f, fullfile(figDir, 'basic5_implicit_solution.png'), 'Resolution', 180);
f = figure('Visible', 'off');
plot(yi(:,1), yi(:,3), 'LineWidth', 1.0); grid on;
xlabel('x_1'); ylabel('x_2'); title('Implicit ODE phase curve');
exportgraphics(f, fullfile(figDir, 'basic5_implicit_phase.png'), 'Resolution', 180);

%% Basic experiment 6
lags = [1 0.5];
solDde = dde23(@ddeRhs, lags, @exer1h, [0 1]);
tDde = linspace(0, 1, 200);
yDde = deval(solDde, tDde);
fprintf('\n[Basic 6]\n');
fprintf('y(1) = [%.10f %.10f %.10f %.10f %.10f]\n', yDde(:,end));
f = figure('Visible', 'off');
plot(tDde, yDde, 'LineWidth', 1.0); grid on;
xlabel('t'); ylabel('y_i(t)'); title('DDE solution on [0,1]');
legend('y_1','y_2','y_3','y_4','y_5','Location','best');
exportgraphics(f, fullfile(figDir, 'basic6_dde.png'), 'Resolution', 180);

%% Basic experiment 7
fprintf('\n[Basic 7]\n');
fprintf('No feasible c: x(0)=y(0)=0 is an equilibrium for every c, so y(5)=1 cannot be satisfied.\n');
try
    solinit = bvpinit(linspace(0, 5, 30), @bvpGuess, 1);
    solBvp = bvp4c(@bvpRhs, @bvpBc, solinit, bvpset('RelTol', 1e-6, 'AbsTol', 1e-8));
    tBvp = linspace(0, 5, 200);
    yBvp = deval(solBvp, tBvp);
    cValue = solBvp.parameters;
    fprintf('Unexpected numerical solution: c = %.10f, y(5)=%.10f\n', cValue, yBvp(2,end));
catch ME
    fprintf('bvp4c diagnostic: %s\n', ME.message);
    tBvp = linspace(0, 5, 200);
    yBvp = zeros(2, numel(tBvp));
end
f = figure('Visible', 'off');
plot(tBvp, yBvp(1,:), tBvp, yBvp(2,:), 'LineWidth', 1.0); hold on;
yline(1, '--', 'y(5) target'); grid on;
xlabel('t'); ylabel('solution'); title('BVP diagnostic: equilibrium cannot reach y(5)=1');
legend('x(t)','y(t)','Location','best');
exportgraphics(f, fullfile(figDir, 'basic7_bvp_diagnostic.png'), 'Resolution', 180);

%% Exploratory experiment 1, Hodgkin-Huxley
hhY0 = [0.5; 0.5; 0.5; -60];
[thh, yhh] = ode45(@hhRhs, [0 20], hhY0, opts);
ySS = yhh(end, :)';
fprintf('\n[Exploratory 1 - Hodgkin-Huxley]\n');
fprintf('ySS = [%.10f %.10f %.10f %.10f]\n', ySS);
f = figure('Visible', 'off');
plot(thh, yhh(:,4), 'k', 'LineWidth', 1.1); grid on;
xlabel('Time (ms)'); ylabel('Transmembrane voltage (mV)');
title('Approaching steady state');
exportgraphics(f, fullfile(figDir, 'explore1_hh_steady.png'), 'Resolution', 180);
f = figure('Visible', 'off'); hold on; grid on;
peakValues = zeros(10, 1);
firstSpike = NaN;
for d = 1:10
    yStart = ySS;
    yStart(4) = yStart(4) + d;
    [tt, yy] = ode45(@hhRhs, [0 20], yStart, opts);
    peakValues(d) = max(yy(:,4));
    if peakValues(d) > 0
        color = 'r';
        if isnan(firstSpike)
            firstSpike = d;
        end
    else
        color = 'k';
    end
    plot(tt, yy(:,4), color, 'LineWidth', 0.9);
end
xlabel('Time (ms)'); ylabel('Transmembrane voltage (mV)');
title('Threshold behavior');
exportgraphics(f, fullfile(figDir, 'explore1_hh_threshold.png'), 'Resolution', 180);
fprintf('peak voltages for delta=1..10 mV:\n');
fprintf('%.8f ', peakValues); fprintf('\n');
fprintf('first delta with peak > 0 mV = %.0f\n', firstSpike);
diary off;
end

function dydt = apolloRhs(~, y)
mu = 1 / 82.45;
mu1 = 1 - mu;
x = y(1); vx = y(2); yy = y(3); vy = y(4);
r1 = sqrt((x + mu)^2 + yy^2);
r2 = sqrt((x - mu1)^2 + yy^2);
dydt = [vx;
        2 * vy + x - mu1 * (x + mu) / r1^3 - mu * (x - mu1) / r2^3;
        vy;
        -2 * vx + yy - mu1 * yy / r1^3 - mu * yy / r2^3];
end

function dydt = pendulumRhs(~, u)
g = 9.81;
d = u(1) - u(2);
A = [2, cos(d); cos(d), 1];
b = [-g * sin(u(1)) - sin(d) * u(4)^2;
     -g * sin(u(2)) + sin(d) * u(3)^2];
a = A \ b;
dydt = [u(3); u(4); a(1); a(2)];
end

function res = implicitResidual(t, y, yp)
x1 = y(1); v1 = y(2); x2 = y(3); v2 = y(4);
a1 = yp(2); a2 = yp(4);
res = [yp(1) - v1;
       yp(3) - v2;
       v1 * a2 * sin(x1 * x2) + 5 * a1 * v2 * cos(x1^2) + t^2 * x1 * x2^2 - exp(-x2^2);
       a1 * x2 + a2 * v1 * sin(x1^2) + cos(a2 * x2) - sin(t)];
end

function dydt = ddeRhs(~, ~, Z)
lag1 = Z(:,1);
lagHalf = Z(:,2);
dydt = [lag1(5) + lag1(3);
        lag1(1) + lagHalf(2);
        lag1(3) + lagHalf(1);
        lag1(5) * lag1(4);
        lag1(1)];
end

function y = bvpGuess(t)
y = [t / 5; t / 5];
end

function dydt = bvpRhs(~, y, p)
c = p(1);
dydt = [y(1)^2 - y(2);
        (y(1) - y(2)) * (y(1) - y(2) - c)];
end

function r = bvpBc(ya, yb, ~)
r = [ya(1); ya(2); yb(2) - 1];
end

function dydt = hhRhs(~, y)
n = y(1); m = y(2); h = y(3); V = y(4);
C = 1;
GK = 36;
GNa = 120;
GL = 0.3;
EK = -72;
ENa = 55;
EL = -49.4;
dydt = [(1 - n) * alphan(V) - n * betan(V);
        (1 - m) * alpham(V) - m * betam(V);
        (1 - h) * alphah(V) - h * betah(V);
        -(GK * n^4 * (V - EK) + GNa * m^3 * h * (V - ENa) + GL * (V - EL)) / C];
end
