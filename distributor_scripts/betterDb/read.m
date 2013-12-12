data = csvread('test_log_20.txt');
time = data(:,1)./1000./60;
write = data(:,2);
reads = data(:,3);
total = write + reads;

ops = data(:,4);
io = data(:,5)./ops;
crw = data(:,6)./ops;
db = data(:,7)./ops;

responseTime = io./1000000;

prctlWrite = quantile(write,[0.05 0.95]);
prctlRead = quantile(reads,[0.05 0.95]);
prctlRT = quantile(responseTime,[0.05 0.95]);


fprintf('----------------------\n')
fprintf('Mean write : %4.2f (%3.2f , %4.2f) [%3.2f]\n',mean(write),...
    prctlWrite-mean(write), sum(prctlWrite)/2);

fprintf('Mean read : %4.2f (%3.2f , %3.2f) [%3.2f]\n',mean(reads),...
    prctlRead-mean(reads), sum(prctlRead)/2);

fprintf('Mean response time : %4.2f (%3.2f , %3.2f) [%3.2f]\n',mean(responseTime),...
    prctlRT-mean(responseTime), sum(prctlRT)/2);

fprintf('Breakdown Response Time : %4.2f %4.2f %4.2f \n',...
    mean(io-crw)./1000000, mean(crw-db)./1000000,mean(db)./1000000);


subplot(1,2,1);
hold off
plot(time,write,'+')
hold all
plot(time, reads,'.')
xlabel 'Time / Minutes'
ylabel 'Throughput / Messages per second'
legend 'Push' 'Pop'
hold off
subplot(1,2,2);
plot(time,responseTime,'.')
xlabel 'Time / Minutes'
ylabel 'Response Time / ms'