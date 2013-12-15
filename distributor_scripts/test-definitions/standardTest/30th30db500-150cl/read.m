data = csvread('test_log.txt',0,0);
data = data(:,:);
time = data(:,1)./1000./60;
writes = data(:,2)./5;
reads = data(:,3)./5;
total = writes+reads;
totalStd = std(total);
readStd = std(reads);
writesStd = std(writes);

plot(time, writes,'.', time, reads,'.');

hm = graph2d.constantline(mean(reads), 'LineStyle',':', 'Color','red');
changedependvar(hm,'y');
hsl = graph2d.constantline(mean(reads)-readStd, 'LineStyle','-', 'Color','red');
changedependvar(hsl,'y');
hsu = graph2d.constantline(mean(reads)+readStd, 'LineStyle','-', 'Color','red');
changedependvar(hsu,'y');

hm = graph2d.constantline(mean(writes), 'LineStyle',':', 'Color','red');
changedependvar(hm,'y');
hsl = graph2d.constantline(mean(writes)-writesStd, 'LineStyle','-', 'Color','red');
changedependvar(hsl,'y');
hsu = graph2d.constantline(mean(writes)+writesStd, 'LineStyle','-', 'Color','red');
changedependvar(hsu,'y');

xlabel Minutes
ylabel 'Throughput / Messages per Second'
legend('Push', 'Pop')
disp(strcat('500 & 250 & 30/30 &', num2str(mean(total)),' & ', num2str(totalStd)));

responsetimes = data(:,5)./data(:,4);

[mu, ~, muci, ~] = normfit(data(:,2)./5);
disp(strcat('Push thrpt: ',num2str(mu),'±',num2str(mu-muci(1))));
[mu, ~, muci, ~] = normfit(data(:,3)./5);
disp(strcat('Pop thrpt: ',num2str(mu),'±',num2str(mu-muci(1))));

respTime = (data(:,5)./data(:,4))./10000000;
respTime = respTime(all(~isnan(respTime),2),:);
[mu, ~, muci, ~] = normfit(respTime);
disp(strcat('ResponseTime: ',num2str(mu),'±',num2str(mu-muci(1))));
