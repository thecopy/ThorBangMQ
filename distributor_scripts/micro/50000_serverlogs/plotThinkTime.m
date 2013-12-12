testdataPush = csvread('push.log',0,0);
testdataPop = csvread('pop.log',0,0);
testdataPeek = csvread('peek.log',0,0);
close all
opsPush=testdataPush(:,4);
rtPush =testdataPush(:,5)./testdataPush(:,4)./1000000;
ioPush=(testdataPush(:,5)-testdataPush(:,6))./1000000;
crwPush=(testdataPush(:,6)-testdataPush(:,7))./1000000;
dbPush=testdataPush(:,7)./1000000;

opsPeek=testdataPeek(:,4);
rtPeek =testdataPeek(:,5)./testdataPeek(:,4)./1000000;
ioPeek=(testdataPeek(:,5)-testdataPeek(:,6))./1000000;
crwPeek=(testdataPeek(:,6)-testdataPeek(:,7))./1000000;
dbPeek=testdataPeek(:,7)./1000000;

opsPop=testdataPop(:,4);
rtPop =testdataPop(:,5)./testdataPop(:,4)./1000000;
ioPop=(testdataPop(:,5)-testdataPop(:,6))./1000000;
crwPop=(testdataPop(:,6)-testdataPop(:,7))./1000000;
dbPop=testdataPop(:,7)./1000000;

thinkTimes = [sum(ioPush)./sum(opsPush) sum(crwPush)./sum(opsPush) sum(dbPush)./sum(opsPush);...
              sum(ioPeek)./sum(opsPeek) sum(crwPeek)./sum(opsPeek) sum(dbPeek)./sum(opsPeek);...
              sum(ioPop)./sum(opsPop) sum(crwPop)./sum(opsPop) sum(dbPop)./sum(opsPop)];
figure(1)
bar([1 2 3], thinkTimes, 'stack')
hold on
errorbar([1 2 3], sum(thinkTimes,2), [std(rtPush) std(rtPeek) std(rtPop)],'black.');
ylabel 'Mean Think Time / ms'

figure(2)
subplot(1,5,1)
rtPush = rtPop;
hist(rtPush(1:length(rtPush)/5),100)
%xlabel 'Mean Think Time / ms'
ylabel 'Count'
axis([0 5 0 35])

subplot(1,5,2)
hist(rtPush(length(rtPush)/5:2*length(rtPush)/5),100)
%xlabel 'Mean Think Time / ms'
ylabel 'Count'
axis([0 5 0 35])


subplot(1,5,3)
hist(rtPush(2*length(rtPush)/5:3*length(rtPush)/5),100)

%xlabel 'Mean Think Time / ms'
ylabel 'Count'
axis([0 5 0 35])


subplot(1,5,4)
hist(rtPush(3*length(rtPush)/5:4*length(rtPush)/5),100)

%xlabel 'Mean Think Time / ms'
ylabel 'Count'
axis([0 5 0 35])

subplot(1,5,5)
hist(rtPush(4*length(rtPush)/5:end),100)

%xlabel 'Mean Think Time / ms'
ylabel 'Count'
axis([0 5 0 35])


pause
hist(rtPeek,100)
xlabel 'Mean Think Time / ms'
ylabel 'Count'
pause
hist(rtPop,100)
xlabel 'Mean Think Time / ms'
ylabel 'Count'