%MVA(M,N,Z,V,S,T)

M = 4; 
m = [1, 1, 10, 10]; %num servers in M/M/m: IS, M/M/10
Z = 0.1;
N = 1000;
V = [0, 2, 1, 1]; 
S = [0, 0.0001, 0.0001, 0.0043]; %Think Time, Network, CRW, DB

u_max = (0.1329*m(4)^1.235 + 2.099)./1000;
%u_max = 0.00445;
u = [NaN.*ones(1,N);
     NaN.*ones(1,N);
     NaN.*ones(1,N);
     u_max.*ones(1,N)];
 
u(4,1:m(4)) = (0.1329.*(1:m(4)).^1.235 + 2.099)./1000;

T = [2, 2, 1, 1]; % 1 = Load Independent, 2 = Infinity Server, 3 = Load Dependent

[X,Q] = MVA(M,m,N,Z,V,S,u,T);

fprintf('MVA Completed for N = %d\tX = %2.1f - %2.1f\n',N, X(1), X(end)); 

subplot(3,1,1);
plot(1:N,X);
title Throughput
grid on

subplot(3,1,2);
plot(1:N,(1:N)./X-Z);
title ResponseTime
grid on

subplot(3,1,3);
hold off
U = X.*u(4,:)*V(4)/m(4);
plot(1:N,U);
hold all
U = X.*S(3)*V(3);
plot(1:N,U);
hold off
legend 'DB' 'CRW'
legend('Location','NorthWest')
title Utiliziation
grid on