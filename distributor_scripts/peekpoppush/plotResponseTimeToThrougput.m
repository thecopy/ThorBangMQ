measurements = length(performance);

throughput = zeros(measurements, 1);
pop = zeros(measurements, 1);
push = zeros(measurements, 1);
peek = zeros(measurements, 1);

for i=1:measurements
    pop(i) = mean(performance(i).pop);
    peek(i) = mean(performance(i).peek);
    push(i) = mean(performance(i).push);
    throughput(i) = mean(performance(i).throughut);
end

set(0, 'DefaultAxesFontSize',14)

plot(throughput, push, '-*r',...
    throughput, peek, '-+b',...
    throughput, pop, '-og')

legend Push Peek Pop

xlabel 'Throughput / Messsages per Second'
ylabel 'Response Time / ms'