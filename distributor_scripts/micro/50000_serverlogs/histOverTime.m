testdata = csvread('peek.log',0,0);
testdata = testdata(1:end,:);
time = testdata(:,1)./1000.*2./60;
time = time-min(time);

ops=testdata(:,4);
responseTime = testdata(:,5)./1000000./ops;
io=(testdata(:,5)-testdata(:,6))./1000000./ops;
crw=(testdata(:,6)-testdata(:,7))./1000000./ops;
db=testdata(:,7)./1000000./ops;
close all
counts = 6;
means = zeros(4, counts);
stds = zeros(1, counts);
for i = 0:counts-1
   len = length(io)/counts;
   respPart = responseTime(round(i*len+1):round((i+1)*len));
   ioPart = io(round(i*len+1):round((i+1)*len));
   crwPart = crw(round(i*len+1):round((i+1)*len));
   dbPart = db(round(i*len+1):round((i+1)*len));
   
   [RTelements,RTcenters] = hist(respPart,50);
   [IOelements,IOcenters] = hist(ioPart,50);
   [CRWelements,CRWcenters] = hist(crwPart,50);
   [DBelements,DBcenters] = hist(dbPart,50);
   means(i+1,1) = mean(respPart);
   stds(i+1,1) = std(respPart);
   means(i+1,2) = mean(ioPart);
   stds(i+1,2) = std(ioPart);
   means(i+1,3) = mean(crwPart);
   stds(i+1,3) = std(crwPart);
   means(i+1,4) = mean(dbPart);
   stds(i+1,4) = std(dbPart);
end

%pop
%msgs = 1.0e+06*[0.2000    1.0333    1.8667    2.7000    3.5333    4.3667];
%push
%msgs = 20000 +5000000/6 .* [1 2 3 4 5 6];
%peek
msgs = max(time)./6 .* [1 2 3 4 5 6];

subplot(2,2,1);
hold on
bar(msgs,means(:,1))
errorbar(msgs,means(:,1), stds(:,1), 'black*')
legend 'Mean Think Time'
%xlabel 'Messages In Db'
xlabel 'Time / Minutes'
ylabel 'Think Time / ms'

subplot(2,2,2);
hold on
bar(msgs,means(:,2))
errorbar(msgs,means(:,2), stds(:,2), 'black*')
legend 'Mean Think Time'
%xlabel 'Messages In Db'
xlabel 'Time / Minutes'
ylabel 'Think Time / ms'

subplot(2,2,3);
hold on
bar(msgs,means(:,3))
errorbar(msgs,means(:,3), stds(:,3), 'black*')
legend 'Mean Think Time'
%xlabel 'Messages In Db'
xlabel 'Time / Minutes'
ylabel 'Think Time / ms'

subplot(2,2,4);
hold on
bar(msgs,means(:,4))
errorbar(msgs,means(:,4), stds(:,4), 'black*')
legend 'Mean Think Time'
%xlabel 'Messages In Db'
xlabel 'Time / Minutes'
ylabel 'Think Time / ms'