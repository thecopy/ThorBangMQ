clientPush = csvread('500k_peek.log');
serverPush = csvread('peek.log');
clientPush = sort(clientPush(:,2));
clientPush = clientPush(1:end-3000);
serverPush = sort(serverPush(:,5)./serverPush(:,4)./1000000);
serverPush = serverPush(1:end-50);

meanServerPush = mean(serverPush);
meanClientPush = mean(clientPush);

meanServerPushPrctl =  quantile(serverPush,[.05, .95]);
meanClientPushPrctl =  quantile(clientPush,[.05, .95]);

diffPush = meanClientPush - meanServerPush;

meanDiffPushPrctl =  [meanClientPushPrctl(1) - meanServerPushPrctl(1);
                      meanClientPushPrctl(2) + meanServerPushPrctl(2)];
             figure(1);     
subplot(1,2,1);
plot(clientPush,'.');
ylabel 'Response Time / ms'
xlabel 'Request #'
subplot(1,2,2);
plot(linspace(1,500000, length(serverPush)),serverPush,'.');
ylabel 'Response Time / ms'
xlabel 'Request #'


figure(2)   
subplot(2,1,1);
hist(serverPush,200)   
ylabel 'Count'
xlabel 'Response Time / ms'
subplot(2,1,2);
hist(clientPush,20000) 
ylabel 'Count'
xlabel 'Response Time / ms'
