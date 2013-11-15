data = csvread('test1.1_server0_test.txt');
time = data(:,1)./1000./60;
write = data(:,2);
reads = data(:,3);
total = write + reads;
responseTime = data(:,5)./data(:,4)./1000000;
prctlWrite = quantile(write,[0.05 0.95]);
prctlRead = quantile(reads,[0.05 0.95]);
prctlRT = quantile(responseTime,[0.05 0.95]);
prctlTotal = quantile(total,[0.05 0.95]);

fprintf('----------------------\n')
fprintf('Mean write : %4.2f (%3.2f , %4.2f) [%3.2f]\n',mean(write),...
    prctlWrite-mean(write), sum(prctlWrite)/2);

fprintf('Mean read : %4.2f (%3.2f , %3.2f) [%3.2f]\n',mean(reads),...
    prctlRead-mean(reads), sum(prctlRead)/2);

fprintf('Mean total : %4.2f (%3.2f , %3.2f) [%3.2f]\n',mean(total),...
    prctlTotal-mean(total), sum(prctlTotal)/2);

fprintf('Mean response time : %4.2f (%3.2f , %3.2f) [%3.2f]\n',mean(responseTime),...
    prctlRT-mean(responseTime), sum(prctlRT)/2);


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