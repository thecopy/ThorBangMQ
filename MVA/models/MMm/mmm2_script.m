loadRealData;
m = 10;
serviceRate = (throughputB+10)./m;
arrivalRates = throughputB;

E_r = zeros(1,length(arrivalRates));
E_n = zeros(1,length(arrivalRates));
E_q = zeros(1,length(arrivalRates));

p = zeros(1,length(arrivalRates));
p0 = zeros(1,length(arrivalRates));
g = zeros(1,length(arrivalRates));

for i=1:length(arrivalRates)
    [E_r(i), E_n(i), E_q(i), p(i), p0(i), g(i)] = mmm2(arrivalRates(i),serviceRate(i),m);
end

close all
subplot(2,1,1)
plot(clients, E_r.*1000, clients, respTime.*1000);
ylabel 'Response Time [ms]'
xlabel 'Clients'
legend 'Model' 'Real'

subplot(2,1,2)
plot(clients, clients./(0.2+E_r), clients, throughput);
ylabel 'Throughput'
xlabel 'Clients'
legend 'Model' 'Real'
legend('Location','NorthWest')