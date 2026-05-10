function s = exer1h(t)
% History function for the delay differential equation in Basic Experiment 6.
s = [exp(t + 1);
     exp(t + 0.5);
     sin(t + 1);
     exp(t + 1);
     exp(t + 1)];
end
