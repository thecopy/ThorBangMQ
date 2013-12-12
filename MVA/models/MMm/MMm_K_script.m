arrivalRates = linspace(10,3000,20);
serviceRate = linspace(1/(3/800),1/(3/799),20);
client = 1:20;
arrivalRates = 1310 .* atan(0.2141.*clients) + 40.8;

E_r = zeros(1,length(arrivalRates));
E_n = zeros(1,length(arrivalRates));
E_q = zeros(1,length(arrivalRates));

m = 10;
K = 64;
for i=1:length(arrivalRates)
    [E_r(i), E_n(i), E_q(i)] = MMm_K(arrivalRates(i), serviceRate(i), m, K);
end
close all
subplot(2,1,1)
plot(clients, E_r.*1000);
ylabel 'Response Time [ms]'
xlabel 'Arrival Rate'

subplot(2,1,2)
plot(clients, E_n./E_r);
ylabel 'Number'
xlabel 'Arrival Rate'
legend 'Throughput'

