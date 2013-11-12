testdata = csvread('thinkTime_pop.log',0,3);
testdata = sum(testdata,1);
ops=testdata(1);
total=testdata(2)./1000./1000;
crw=testdata(3)./1000./1000;
db=testdata(4)./1000./1000;

avgTot = total/ops;
avgCrw = crw/ops;
avgDb = db/ops;
disp(strcat('Avg Total Think Time:', num2str(avgTot)))
disp(strcat('Avg CRW Think Time:', num2str(avgCrw)))
disp(strcat('Avg Peristence Think Time:', num2str(avgDb)))