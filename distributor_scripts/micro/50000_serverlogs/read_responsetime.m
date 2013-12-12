%100 worker threads, 100 db threads
testdata = csvread('500k_push.log',0,0);
plot(testdata(:,2),'.')
hold on

stdVal = std(testdata(:,2));

hm = graph2d.constantline(mean(testdata(:,2)), 'LineStyle',':', 'Color','black');
changedependvar(hm,'y');
hsl = graph2d.constantline(mean(testdata(:,2))-stdVal, 'LineStyle','-', 'Color','black');
changedependvar(hsl,'y');
hsu = graph2d.constantline(mean(testdata(:,2))+stdVal, 'LineStyle','-', 'Color','black');

hold off
xlabel 'Request #'
ylabel 'Response Time'
mean(testdata(:,2))
[mu, ~, muci, ~] = normfit(testdata(:,2));
%disp(strcat('Responsetime: ',num2str(mu),'±',num2str(mu-muci(1))));