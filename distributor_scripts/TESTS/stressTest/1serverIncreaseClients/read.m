dataPoints = [[1 61]; [1 61]; [22 1222]; [25 1250]; [34 1210]];
intervals = [10 10 0.5];
clients = [1 2 3 4 5 6 7];
%avergage and mean for 1 client
client1 = csvread('1clients1.log',0,1);
clients22 = csvread('2clients1-2.log',0,1);
clients21 = csvread('2clients1-1.log',0,1);
clients3 = csvread('3clients1.log',0,1);
clients4 = csvread('4clients1.log',0,1);
clients5 = csvread('5clients1.log',0,1);
clients6 = csvread('6clients1.log',0,1);
clients7 = csvread('7clients1.log',0,1);
clients241 = csvread('4clients2-1.log',0,1);
clients242 = csvread('4clients2-2.log',0,1);
clients261 = csvread('6clients2-1.log',0,1);
clients262 = csvread('6clients2-2.log',0,1);
clients221 = csvread('2clients2-1.log',0,1);
clients222 = csvread('2clients2-2.log',0,1);
throughput1 = (client1(3:59,1)+client1(3:59,2))./10;
throughput2 = (clients22(35:1200,1)+clients22(35:1200,2)).*2;
throughput3 = (clients3(25:1210,1)+clients3(25:1210,2)).*2;
throughput4 = (clients4(25:1210,1)+clients4(25:1210,2)).*2;
throughput5 = (clients5(25:1210,1)+clients5(25:1210,2)).*2;
throughput6 = (clients6(25:1210,1)+clients6(25:1210,2)).*2;
throughput7 = (clients7(25:1210,1)+clients7(25:1210,2)).*2;
throughput24 = (clients241(35:1210,1)+clients241(35:1210,2)).*2 ...
             + (clients241(35:1210,1)+clients241(35:1210,2)).*2;
throughput26 = (clients261(35:1210,1)+clients261(35:1210,2)).*2 ...
             + (clients261(35:1210,1)+clients261(35:1210,2)).*2;
throughput22 = (clients221(35:1210,1)+clients221(35:1210,2)).*2 ...
             + (clients221(35:1210,1)+clients221(35:1210,2)).*2;
         
throughput = [mean(throughput1) mean(throughput2) mean(throughput3) ...
        mean(throughput4) mean(throughput5) mean(throughput6)...
        mean(throughput7)];
throughput2 = [mean(throughput22) mean(throughput24) mean(throughput26)];
ops1 = sum(client1(:,3));
ops2 = sum(clients2(:,3));
ops3 = sum(clients3(:,3));
ops4 = sum(clients4(:,3));
ops5 = sum(clients5(:,3));
ops6 = sum(clients6(:,3));
ops7 = sum(clients7(:,3));

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
hold off
errorbar(clients,throughput,...
    [std(throughput1) std(throughput2) std(throughput3) ...
        std(throughput4) std(throughput5) std(throughput5)...
        std(throughput7)],'bo-');
hold on
errorbar([2 4 6],throughput2,...
    [std(throughput22) std(throughput24) std(throughput26)], 'r*-');
hold off

legend '1 Middleware' '2 Middlewares'
    
xlabel 'Clients'
ylabel 'Throughput / Average Messages per Second'
pause
bar(thinkTime7./10000000, 'stack')
xlabel 'Throughput / Average Messages per Second'
ylabel 'Mean Think Time / ms'
legend 'Socket I/O' 'Client Request Worker' 'IPersistence'
legend('Location','NorthWest')
    