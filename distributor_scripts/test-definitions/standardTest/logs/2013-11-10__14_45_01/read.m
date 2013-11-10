threads = [20 40 60 80 100];
reads = zeros(5,6);
writes = zeros(5,6);

for i=1:5
    data = csvread(strcat('test',num2str(i),'.1_server0_test.txt'),0,1);
    writes(i,:) = data(:,1)';
    reads(i,:) = data(:,2)';
end

errorbar(threads, mean(writes'+reads'),std(writes'+reads'), 'r*-')
hold on
errorbar(threads, mean(reads'),std(reads'), 'b+-')
errorbar(threads, mean(writes'),std(writes'), 'go-')
hold off
axis([0 120 0 5500]);
xlabel Worker-Threads
ylabel 'Messages per 10 seconds'
legend Total Reads Writes
