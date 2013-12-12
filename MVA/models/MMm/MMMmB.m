% See 538-539 in book
% M/M/m//K queue

%% Measured Data
dataPoints = 1:64;
ioServiceTime = 0.0004.*(dataPoints);
crwServiceTime = 0.00003.*(dataPoints);
dbServiceTime = 0.001643.*atan(0.2028.*(dataPoints)) + 0.001662;

serviceTime = ioServiceTime + crwServiceTime + dbServiceTime;
serviceRate = 1./serviceTime;

arrivalRate = 1320 .* atan(0.2141.*dataPoints) + 40.8;
m = 10;
Z = 0;
B = 64;

p = arrivalRate./(m.*serviceRate);

%% Calculate p0
p0 = 1 + ((1-(p.^(B-m+1))) .* (m.*p).^m) ./ (factorial(m) * (1-p));
for n=1:m-1
    p0 = p0 + (m.*p).^n./(factorial(n));
end
p0 = 1./p0;

%% Calculate p_n
p_n = zeros(B,B);
for n=0:B
    if n < m
        p_n(n+1,:) = 1/factorial(n) .* (m.*p).^n .* p0;
    else
        p_n(n+1,:) = (m^m .* p.^n)./factorial(m) .* p0;
    end
end


%% Calculate arr_eff
arr_eff = zeros(1,B);
for n=0:B-1
    arr_eff(n+1) = arrivalRate(n+1).*(1-p_n(B+1,n+1));
end

%% Calculate E[n]
% for m=1  E_n = p/(1-p)-((B+1).*p.^(B+1))./((1-B).^(B+1));
E_n = 0;
for n=1:B+1
    E_n = E_n + n.*p_n(n,:);
end
%% Calculate E[q]
% for m=1  E_n = p/(1-p)-((B+1).*p.^(B+1))./((1-B).^(B+1));
E_q = 0;
for n=m+1:B
    E_q = E_q + (n-m).*p_n(n+1,:);
end


%% Calculate Performance

threadAvgUtilization = arr_eff./(m.*serviceRate);
meanResponseTime = E_n ./ arr_eff;
meanThroughput = (dataPoints)./(meanResponseTime + Z);


%run(strcat('../../../distributor_scripts/test-definitions/distributedStressTest'...
%    ,'/logs/10min1-64cl2mw-2/readTests'));

close all
subplot(2,2,1);
plot(dataPoints,meanThroughput,clientNum,results(:,2),1:64,arrivalRate,1:64,arr_eff)
legend 'Model' 'Real' '\lambda' '\lambda'''
title 'Throughput'
subplot(2,2,2);
plot(dataPoints,meanResponseTime.*1000,clientNum,results(:,1));
title 'Response Time'
subplot(2,2,3);
plot(1:64, E_n, 1:64, E_q, 1:64, E_n-E_q);
legend 'E[n]' 'E[n_q]' 'diff'
legend('Location','East')
title 'Expectations'
subplot(2,2,4);
plot(threadAvgUtilization);
title 'Utilization'