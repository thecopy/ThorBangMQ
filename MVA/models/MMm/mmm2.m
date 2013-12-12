% See 528-529 in book
% M/M/m queue
function [E_r, E_n, E_q, p, p0, g] = mmm2(arrivalRate, serviceRate, m)
%% Measured Data
p = arrivalRate/(m*serviceRate);
if(p > 1)
    error(sprintf('Utilization = %2.4f > 1',p));
end
%% Calculate p0
p0 = 1 + (m*p)^m / (factorial(m) * (1-p));
for n=1:m-1
    p0 = p0 + (m*p)^n/(factorial(n));
end
p0 = 1/p0;

%% Calculate g
g = (m*p)^m / (factorial(m) * (1-p)) * p0;

%% Calculate Performance
E_r = 1/serviceRate * (1 + g/(m*(1-p)));
E_q = p*g / (1-p);
E_n = m*p + p*g/(1-p);
