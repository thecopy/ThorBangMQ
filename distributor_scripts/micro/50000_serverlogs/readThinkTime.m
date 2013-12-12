testdata = csvread('peek.log',0,0);
testdata = testdata(1:end,:);
time = testdata(:,1)./1000.*2./60;
time = time-min(time);

msgs = cumsum(testdata(:,2))+20000;

ops=testdata(:,4);
responseTime = testdata(:,5)./testdata(:,4)./1000000;
io=(testdata(:,5)-testdata(:,6))./1000000;
crw=(testdata(:,6)-testdata(:,7))./1000000;
db=testdata(:,7)./1000000;
figure(1);
hold off
hold all
%dataPoints = linspace(520000,20000, length(ops)); use for pop and push
h = plot(time, io./ops,'.');
plot(time, crw./ops,'.');
plot(time, db./ops,'.');
 set(h, 'Markersize',6);
%addaxis(time, msgs, 'black--');
%addaxislabel(2,'# of Messages in Db');
hold off
legend('Socket I/O*', 'Client Request Worker','Db');
legend('Location','NorthEast');
 xlabel 'Time / Minutes'
 ylabel 'Think Time / ms'
 
 prctlOps = quantile(ops.*2,[.05, .95]);
 prctlIO = quantile(io./ops,[.05, .95]);
 prctlCRW = quantile(crw./ops,[.05, .95]);
 prctlDB = quantile(db./ops,[.05, .95]);
prctlRT = quantile(responseTime,[0.05 0.95]);
disp('------------------------------');
fprintf('Mean Throughut : %4.4f [%4.4f , %4.4f] [%4.4f]\n',mean(ops).*2,...
    prctlOps-mean(ops).*2, sum(prctlOps)/2);

fprintf('Mean response time : %4.4f [%4.4f , %4.4f] [%4.4f]\n',mean(responseTime),...
    prctlRT-mean(responseTime), sum(prctlRT)/2);

fprintf('Mean Socket IO time : %4.4f [%4.4f , %4.4f] [%4.4f]\n',mean(io./ops),...
    prctlIO-mean(io./ops), sum(prctlIO)/2);

fprintf('Mean CRW time : %4.4f [%4.4f , %4.4f] [%4.4f]\n',mean(crw./ops),...
    prctlCRW-mean(crw./ops), sum(prctlCRW)/2);

fprintf('Mean Persitence time : %4.4f [%4.4f , %4.4f] [%4.4f]\n',mean(db./ops),...
    prctlDB-mean(db./ops), sum(prctlDB)/2);


[mu, ~, muci, ~] = normfit(io./ops);
disp(strcat('Responsetime: ',num2str(mu),'±',num2str(mu-muci(1))));

figure(3)
responsetime = testdata(:,5)./ops./1000000;
plot(time,responsetime,'-');
xlabel 'Time / Minutes'
ylabel 'Reponse Time / ms'

data = testdata;
figure(4);
subplot(2,2,1);
histfit(data(:,3),60)
subplot(2,2,2);
histfit(data(:,5)-data(:,6),60)
subplot(2,2,3);
histfit(data(:,6)-data(:,7),60)
subplot(2,2,4);
histfit(data(:,7),60)

