dataPoints = [[1 61]; [1 61]; [22 1222]; [25 1250]; [34 1210]];
intervals = [10 10 0.5];
clients = [1 2 3 4 5 6 7 8 10 16 20 30 50];
%avergage and mean for 1 client
client1 = csvread('1clients1.log',0,1);
clients2 = csvread('2clients1.log',0,1);
clients3 = csvread('3clients1.log',0,1);
clients4 = csvread('4clients1.log',0,1);
clients5 = csvread('5clients1.log',0,1);
clients6 = csvread('6clients1.log',0,1);
clients7 = csvread('7clients1.log',0,1);
clients8 = csvread('8clients1.log',0,1);
clients10 = csvread('10clients1-1.log',0,1);
clients16 = csvread('16clients1.log',0,1);
clients20 = csvread('20clients1.log',0,1); 
clients30 = csvread('30clients1.log',0,1); 
clients50 = csvread('50clients1-1.log',0,1); 
clients221 = csvread('2clients2-1.log',0,1);
clients222 = csvread('2clients2-2.log',0,1);
clients241 = csvread('4clients2-1.log',0,1);
clients242 = csvread('4clients2-2.log',0,1);
clients261 = csvread('6clients2-1.log',0,1);
clients262 = csvread('6clients2-2.log',0,1);
clients281 = csvread('8clients2-1.log',0,1);
clients282 = csvread('8clients2-2.log',0,1);
clients2101 = csvread('10clients2-1.log',0,1);
clients2102 = csvread('10clients2-2.log',0,1);
clients2161 = csvread('16clients2-1.log',0,1);
clients2162 = csvread('16clients2-2.log',0,1);
clients2201 = csvread('20clients2-1.log',0,1);
clients2202 = csvread('20clients2-2.log',0,1);
clients2301 = csvread('30clients22-1.log',0,1);
clients2302 = csvread('30clients22-2.log',0,1);
clients2501 = csvread('50clients11-1.log',0,1);
clients2502 = csvread('50clients11-2.log',0,1);
throughput1 = (client1(:,1)+client1(:,2))./10;
throughput2 = (clients2(:,1)+clients2(:,2));
throughput3 = (clients3(:,1)+clients3(:,2)).*2;
throughput4 = (clients4(:,1)+clients4(:,2)).*2;
throughput5 = (clients5(:,1)+clients5(:,2)).*2;
throughput6 = (clients6(:,1)+clients6(:,2)).*2;
throughput7 = (clients7(:,1)+clients7(:,2)).*2;
throughput8 = (clients8(:,1)+clients8(:,2)).*2;
throughput10 = (clients10(:,1)+clients10(:,2));
throughput16 = (clients16(:,1)+clients16(:,2)).*2;
throughput20 = (clients20(:,1)+clients20(:,2)).*2;
throughput30 = (clients30(:,1)+clients30(:,2)).*2;
throughput50 = (clients50(:,1)+clients50(:,2));

throughput24 = (clients241(:,1)+clients241(:,2)).*2 ...
             + (clients241(:,1)+clients241(:,2)).*2;
throughput26 = (clients261(:,1)+clients261(:,2)).*2 ...
             + (clients261(:,1)+clients261(:,2)).*2;
throughput22 = (clients221(:,1)+clients221(:,2)).*2 ...
             + (clients221(:,1)+clients221(:,2)).*2;
throughput28 = (clients281(:,1)+clients281(:,2)).*2 ...
             + (clients281(:,1)+clients281(:,2)).*2;
throughput210 = (clients2101(:,1)+clients2101(:,2)).*2 ...
             + (clients2102(:,1)+clients2102(:,2)).*2;
throughput216 = (clients2161(:,1)+clients2161(:,2)).*2 ...
             + (clients2162(:,1)+clients2162(:,2)).*2;
throughput220 = (clients2201(:,1)+clients2201(:,2)).*2 ...
             + (clients2202(:,1)+clients2202(:,2)).*2;
throughput230 = (clients2301(:,1)+clients2301(:,2)).*2 ...
             + (clients2302(:,1)+clients2302(:,2)).*2;
throughput250 = (clients2501(:,1)+clients2501(:,2)).*2 ...
             + (clients2502(:,1)+clients2502(:,2)).*2;
         
throughput = [mean(throughput1) mean(throughput2) mean(throughput3) ...
        mean(throughput4) mean(throughput5) mean(throughput6)...
        mean(throughput7) mean(throughput8) mean(throughput10) ...
        mean(throughput16) mean(throughput20) mean(throughput30)...
        mean(throughput50)];
    
throughput_22 = [mean(throughput22) mean(throughput24)...
    mean(throughput26) mean(throughput28) mean(throughput210)...
    mean(throughput216) mean(throughput220) mean(throughput230)...
     mean(throughput250)];

ops1 = sum(client1(:,3));
ops2 = sum(clients2(:,3));
ops3 = sum(clients3(:,3));
ops4 = sum(clients4(:,3));
ops5 = sum(clients5(:,3));
ops6 = sum(clients6(:,3));
ops7 = sum(clients7(:,3));
ops8 = sum(clients8(:,3));
ops10 = sum(clients10(:,3));
ops16 = sum(clients16(:,3));
ops20 = sum(clients20(:,3));
ops30 = sum(clients30(:,3));
ops50 = sum(clients50(:,3));
clients22 = clients221(:,1:6) + clients222(:,1:6);
clients24 = clients241(:,1:6) + clients242(:,1:6);
clients26 = clients261(:,1:6) + clients262(:,1:6);
clients28 = clients281(:,1:6) + clients282(:,1:6);
clients210 = clients2101(:,1:6) + clients2102(:,1:6);
clients216 = clients2161(:,1:6) + clients2162(:,1:6);
clients220 = clients2201(:,1:6) + clients2202(:,1:6);
clients230 = clients2301(:,1:6) + clients2302(:,1:6);
clients250 = clients2501(:,1:6) + clients2502(:,1:6);

ops22 = sum(clients221(:,3) + clients222(:,3));
ops24 = sum(clients241(:,3) + clients242(:,3));
ops26 = sum(clients261(:,3) + clients262(:,3));
ops28 = sum(clients281(:,3) + clients282(:,3));
ops210 = sum(clients2101(:,3) + clients2102(:,3));
ops216 = sum(clients2161(:,3) + clients2162(:,3));
ops220 = sum(clients2201(:,3) + clients2202(:,3));
ops230 = sum(clients2301(:,3) + clients2302(:,3));
ops250 = sum(clients2501(:,3) + clients2502(:,3));


stdRespTime1 = client1(:,4)./client1(:,3)./1000000;
stdRespTime2 = clients2(:,4)./clients2(:,3)./1000000;
stdRespTime3 = clients3(:,4)./clients3(:,3)./1000000;
stdRespTime4 = clients4(:,4)./clients4(:,3)./1000000;
stdRespTime5 = clients5(:,4)./clients5(:,3)./1000000;
stdRespTime6 = clients6(:,4)./clients6(:,3)./1000000;
stdRespTime7 = clients7(:,4)./clients7(:,3)./1000000;
stdRespTime8 = clients8(:,4)./clients8(:,3)./1000000;
stdRespTime10 = clients10(:,4)./clients10(:,3)./1000000;
stdRespTime16 = clients16(:,4)./clients16(:,3)./1000000;
stdRespTime20 = clients20(:,4)./clients20(:,3)./1000000;
stdRespTime30 = clients30(:,4)./clients30(:,3)./1000000;
stdRespTime50 = clients50(:,4)./clients50(:,3)./1000000;

stdRespTime22 = clients22(:,4)./clients22(:,3)./1000000;
stdRespTime24 = clients24(:,4)./clients24(:,3)./1000000;
stdRespTime26 = clients26(:,4)./clients26(:,3)./1000000;
stdRespTime28 = clients28(:,4)./clients28(:,3)./1000000;
stdRespTime210 = clients210(:,4)./clients210(:,3)./1000000;
stdRespTime216 = clients216(:,4)./clients216(:,3)./1000000;
stdRespTime220 = clients220(:,4)./clients220(:,3)./1000000;
stdRespTime230 = clients230(:,4)./clients230(:,3)./1000000;
stdRespTime250 = clients250(:,4)./clients250(:,3)./1000000;


thinkTime1 = [sum(client1(:,4)-client1(:,5))/ops1 sum(client1(:,5)-client1(:,6))/ops1 sum(client1(:,6))/ops1];
thinkTime2 = [thinkTime1;...
    sum(clients2(:,4)-clients2(:,5))/ops2 sum(clients2(:,5)-clients2(:,6))/ops2 sum(clients2(:,6))/ops2];
thinkTime3 = [thinkTime2;...
    sum(clients3(:,4)-clients3(:,5))/ops3 sum(clients3(:,5)-clients3(:,6))/ops3 sum(clients3(:,6))/ops3];
thinkTime4 = [thinkTime3;...
    sum(clients4(:,4)-clients4(:,5))/ops4 sum(clients4(:,5)-clients4(:,6))/ops4 sum(clients4(:,6))/ops4];
thinkTime5 = [thinkTime4;...
    sum(clients5(:,4)-clients5(:,5))/ops5 sum(clients5(:,5)-clients5(:,6))/ops5 sum(clients5(:,6))/ops5];
thinkTime6 = [thinkTime5;...
    sum(clients6(:,4)-clients6(:,5))/ops6 sum(clients6(:,5)-clients6(:,6))/ops6 sum(clients6(:,6))/ops6];
thinkTime7 = [thinkTime6;...
    sum(clients7(:,4)-clients7(:,5))/ops7 sum(clients7(:,5)-clients7(:,6))/ops7 sum(clients7(:,6))/ops7];
thinkTime8 = [thinkTime7;...
    sum(clients8(:,4)-clients8(:,5))/ops8 sum(clients8(:,5)-clients8(:,6))/ops8 sum(clients8(:,6))/ops8];
thinkTime10 = [thinkTime8;...
    sum(clients10(:,4)-clients10(:,5))/ops10 sum(clients10(:,5)-clients10(:,6))/ops10 sum(clients10(:,6))/ops10];
thinkTime16 = [thinkTime10;...
    sum(clients16(:,4)-clients16(:,5))/ops16 sum(clients16(:,5)-clients16(:,6))/ops16 sum(clients16(:,6))/ops16];
thinkTime20 = [thinkTime16;...
    sum(clients20(:,4)-clients20(:,5))/ops20 sum(clients20(:,5)-clients20(:,6))/ops20 sum(clients20(:,6))/ops20];
thinkTime30 = [thinkTime20;...
    sum(clients30(:,4)-clients30(:,5))/ops30 sum(clients30(:,5)-clients30(:,6))/ops30 sum(clients30(:,6))/ops30];
thinkTime50 = [thinkTime30;...
    sum(clients50(:,4)-clients50(:,5))/ops50 sum(clients50(:,5)-clients50(:,6))/ops50 sum(clients50(:,6))/ops50];




thinkTime22 = [sum(clients22(:,4)-clients22(:,5))/ops22 sum(clients22(:,5)-clients22(:,6))/ops22 sum(clients22(:,6))/ops22];
thinkTime24 = [thinkTime22;...
    sum(clients24(:,4)-clients24(:,5))/ops24 sum(clients24(:,5)-clients24(:,6))/ops24 sum(clients24(:,6))/ops24];
thinkTime26 = [thinkTime24;...
    sum(clients26(:,4)-clients26(:,5))/ops26 sum(clients26(:,5)-clients26(:,6))/ops26 sum(clients26(:,6))/ops26];
thinkTime28 = [thinkTime26;...
    sum(clients28(:,4)-clients28(:,5))/ops28 sum(clients28(:,5)-clients28(:,6))/ops28 sum(clients28(:,6))/ops28];
thinkTime210 = [thinkTime28;...
    sum(clients210(:,4)-clients210(:,5))/ops210 sum(clients210(:,5)-clients210(:,6))/ops210 sum(clients210(:,6))/ops210];
thinkTime216 = [thinkTime210;...
    sum(clients216(:,4)-clients216(:,5))/ops216 sum(clients216(:,5)-clients216(:,6))/ops216 sum(clients216(:,6))/ops216];
thinkTime220 = [thinkTime216;...
    sum(clients220(:,4)-clients220(:,5))/ops220 sum(clients220(:,5)-clients220(:,6))/ops220 sum(clients220(:,6))/ops220];
thinkTime230 = [thinkTime220;...
    sum(clients230(:,4)-clients230(:,5))/ops230 sum(clients230(:,5)-clients230(:,6))/ops230 sum(clients230(:,6))/ops230];
thinkTime250 = [thinkTime230;...
    sum(clients250(:,4)-clients250(:,5))/ops250 sum(clients250(:,5)-clients250(:,6))/ops250 sum(clients250(:,6))/ops250];

figure(1)
subplot(2,2,[1 2])
hold off
errorbar(clients,throughput,...
    [std(throughput1) std(throughput2) std(throughput3) ...
        std(throughput4) std(throughput5) std(throughput5)...
        std(throughput7) std(throughput8) std(throughput10)...
        std(throughput16) std(throughput20) std(throughput30)...
        std(throughput50)],'bo-');
hold on
errorbar([2 4 6 8 10 16 20 30 50],throughput_22,...
    [std(throughput22) std(throughput24) std(throughput26)...
    std(throughput28) std(throughput210) std(throughput216)...
    std(throughput220) std(throughput230) std(throughput250)], 'r*-');
hold off

legend '1 Middleware' '2 Middlewares'
legend('Location', 'NorthWest');

xlabel '# of Clients'
ylabel 'Throughput / Average Messages per Second'
subplot(2,2,3)
bar(clients, thinkTime50./10000000, 'stack')
xlabel '# of Clients'
ylabel 'Mean Think Time / ms'
%legend 'Socket I/O' 'CRW' 'IPersistence'
%legend('Location','NorthWest')


subplot(2,2,4)
bar([2 4 6 8 10 16 20 30 50], thinkTime250./10000000, 'stack')
xlabel '# of Clients'
ylabel 'Mean Think Time / ms'
%legend 'Socket I/O' 'CRW' 'IPersistence'
%legend('Location','NorthWest')

figure(2)
hold off
%plot(clients,sum(thinkTime50./1000000,2),'bo-');
     hold all
errorbar(clients,sum(thinkTime50./1000000,2),[std(stdRespTime1) std(stdRespTime2) std(stdRespTime3) ...
         std(stdRespTime4) std(stdRespTime5) std(stdRespTime6)...
         std(stdRespTime7) std(stdRespTime8) std(stdRespTime10)...
         std(stdRespTime16) std(stdRespTime20) std(stdRespTime30)...
         std(stdRespTime50)]);
%plot([2 4 6 8 10 16 20 30 50],sum(thinkTime250./1000000,2),'rx-');
errorbar([2 4 6 8 10 16 20 30 50],sum(thinkTime250./1000000,2),...
    [std(stdRespTime22)  ...
         std(stdRespTime24) std(stdRespTime26)...
          std(stdRespTime28) std(stdRespTime210)...
         std(stdRespTime216) std(stdRespTime220) std(stdRespTime230)...
         std(stdRespTime250)],'r*-');
xlabel '# of Clients'
ylabel 'Mean Response Time / ms'
legend '1 middleware' '2 middleware'
legend('Location', 'NorthWest')
hold off

figure(3)
subplot(1,2,1);
bar([2 4 6 8 10 16 20 30 50], thinkTime250(:,1:2)./10000000, 'stack');
xlabel '# of Clients 2 middleware'
ylabel 'Mean Think Time excl. Db/ ms'
subplot(1,2,2);
bar(clients, thinkTime50(:,1:2)./10000000, 'stack');
xlabel '# of Clients 1 middleware'
ylabel 'Mean Think Time excl. Db/ ms'




% 
% for k=1:length(clients)
%     fprintf('%8.2f & %8.2f & %8.2f\\\\ \\hline  \n',...
%         thinkTime7(k,1)./1000,thinkTime7(k,2)./1000,thinkTime7(k,3)./1000);
% end
% 
%     fprintf('%8.2f & %8.2f & %8.2f\\\\ \\hline  \n',...
%         thinkTime22(1,1)./1000,thinkTime22(1,2)./1000,thinkTime22(1,3)./1000);
%     fprintf('%8.2f & %8.2f & %8.2f\\\\ \\hline  \n',...
%         thinkTime24(2,1)./1000,thinkTime24(2,2)./1000,thinkTime24(2,3)./1000);
%     fprintf('%8.2f & %8.2f & %8.2f\\\\ \\hline  \n',...
%         thinkTime26(3,1)./1000,thinkTime26(3,2)./1000,thinkTime26(3,3)./1000);
% 
%     stds;