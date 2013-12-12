dataPoints = [[1 61]; [1 61]; [22 1222]; [25 1250]; [34 1210]];
intervals = [10 10 0.5];
clients = [1 2 3 4 5 6 7 8 10 16 20 30 50];
%avergage and mean for 1 client
clients2501 = csvread('50clients2-1.log',0,0);
clients2502 = csvread('50clients2-2.log',0,0);
clients2501_2 = csvread('50clients11-1.log',0,0);
clients2502_2 = csvread('50clients11-2.log',0,0);

throughput250 = (clients2501(:,2)+clients2501(:,3)).*2 ...
             + (clients2502(:,2)+clients2502(:,3)).*2;
throughput250_2 = (clients2501_2(:,2)+clients2501_2(:,3)).*2 ...
             + (clients2502_2(:,2)+clients2502_2(:,3)).*2;

      close all;   
throughput_22 = [mean(throughput250) mean(throughput250_2)];


clients250 = clients2501(:,1:7) + clients2502(:,1:7);
clients250_2 = clients2501_2(:,1:7) + clients2502_2(:,1:7);

ops250 = sum(clients2501(:,4) + clients2502(:,4));
ops250_2 = sum(clients2501_2(:,4) + clients2502_2(:,4));

resp250 = clients250(:,5)./clients250(:,4)./1000000;
resp250_2 = clients250_2(:,5)./clients250_2(:,4)./1000000;

thinkTime250 = [...
    sum(clients250(:,5)-clients250(:,6))/ops250 sum(clients250(:,6)-clients250(:,7))/ops250 sum(clients250(:,7))/ops250];
thinkTime250_2 = [thinkTime250;...
    sum(clients250_2(:,5)-clients250_2(:,6))/ops250_2 sum(clients250_2(:,6)-clients250_2(:,7))/ops250_2 sum(clients250_2(:,7))/ops250_2];

figure(1)

subplot(1,2,1)
bar([1 2], thinkTime250_2./1000000, 'stack');
hold on
errorbar([1 2], mean(sum(thinkTime250_2,2)./1000000,2), ...
    [std(resp250) std(resp250_2)]);
xlabel 'Run'
ylabel 'Mean Think Time / ms'


subplot(1,2,2)
errorbar([1 2], throughput_22, ...
    [std(throughput250) std(throughput250_2)]);
xlabel 'Run'
ylabel 'Mean Throughput / ms'
%legend 'Socket I/O' 'CRW' 'IPersistence'
%legend('Location','NorthWest')

time1 = clients2501(:,1)./1000;
time2 = clients2501_2(:,1)./1000;
figure(2)
subplot(1,2,1)
plot(time1,resp250)
xlabel 'Time / s'
ylabel 'Response Time / ms'
subplot(1,2,2)
plot(time2,resp250_2)
xlabel 'Time / s'
ylabel 'Response Time / ms'