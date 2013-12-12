m = 30;


ioServiceTime = 0.0004.*(1:64);
crwServiceTime = 0.0003.*(1:64);
dbServiceTime = 0.002639.*atan(0.9902.*(1:64));
S = [ioServiceTime; crwServiceTime; dbServiceTime];
%S = [0.3; 0.2; 0.125];

N = 64;
%N = 20;

Z = 0;
%Z = 4;

M = 3;

V = [1 1 1];
%V = [10 5 16];
Q = zeros(1,M);
R = zeros(1,M);
X = zeros(1,M);
for n=1:N
        R_sum = 0;
        for i=1:M
            R(i) = S(i,n)*(1+Q(i));
            R_sum = R_sum + R(i) * V(i);
        end
        
        X(n) = n/(Z+R_sum);
        
        for i=1:M
            Q(i) = X(n)*V(i)*R(i);
        end
end
close all
plot(1:64,X,clientNum,results(:,2))
