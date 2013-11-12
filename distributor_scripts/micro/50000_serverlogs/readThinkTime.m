testdata = csvread('pop.log',0,0);
testdata = testdata(:,:);
time = testdata(:,1)./1000.*2./60;
time = time-min(time);

msgs = cumsum(-testdata(:,3))+520000;

ops=testdata(:,4);
io=(testdata(:,5)-testdata(:,6))./1000000;
crw=(testdata(:,6)-testdata(:,7))./1000000;
db=testdata(:,7)./1000000;
figure(1);
hold off
plot(time, io./ops,'.',...
     time, crw./ops,'.',...
     time, db./ops,'.')
pause
addaxis(time, msgs, 'black--');
addaxislabel(2,'# of Messages in Db');
 
legend('Socket I/O*', 'Client Request Worker','Db','Msg in Db');
legend('Location','NorthWest');
 xlabel Minutes
 ylabel 'Think Time / ms'
 
disp(strcat('Socket I/O: ', num2str(mean(io)),' ± ', num2str(std(io))));
disp(strcat('CRW: ', num2str(mean(crw)),' ± ', num2str(std(crw))));
disp(strcat('IPersistence: ', num2str(mean(db)),' ± ', num2str(std(db))));

 