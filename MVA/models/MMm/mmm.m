% See 528-529 in book
% M/M/m queue

%% Measured Data
ioServiceTime = 0.0004.*(1:64);
crwServiceTime = 0.00003.*(1:64);
dbServiceTime = 0.001643.*atan(0.2028.*(1:64)) + 0.001662;
dbServiceTime(11:end) = dbServiceTime(10).*ones(1,64-10);

serviceTime = ioServiceTime + crwServiceTime + dbServiceTime;
serviceRate = 1./serviceTime;
%serviceRate = ones(1,64) .* 1/20;

arrivalRate = 1310 .* atan(0.2141.*dataPoints) + 40.8;
%arrivalRate = ones(1,64) .* 1/6;

m = 10;
%m = 5;
Z = 0;
N = 64;

p = arrivalRate./(m.*serviceRate);

%% Calculate p0
p0 = 1 + (m.*p).^m ./ (factorial(m) .* (1-p));
for n=1:m-1
    p0 = p0 + (m.*p).^n./(factorial(n));
end
p0 = 1./p0;

%% Calculate g
g = (m.*p).^m ./ (factorial(m) * (1-p)) .* p0;

%% Calculate p_n
p_n = zeros(N,N);
for n=1:N
    if(n<m)
        p_n(n,:) = p0.* (m.*p).^n ./ factorial(n);
    else
        p_n(n,:) = p0.* p.^n .* m^m ./ factorial(m);
    end
end

threadAvgUtilization = p;

%% Calculate Performance
meanResponseTime = 1./serviceRate .* (1 + g./(m.*(1-p)));
meanThroughput = (1:64)./(meanResponseTime + Z);


%run(strcat('../../../distributor_scripts/test-definitions/distributedStressTest'...
%    ,'/logs/10min1-64cl2mw-2/readTests'));

close all
subplot(1,2,1);
plot(1:64,meanThroughput,'r',clientNum,results(:,2))
legend 'Model' 'Real'
subplot(1,2,2);

plot(1:64,meanResponseTime.*1000,'r',clientNum,results(:,1));
