threads = [30 25 20 15 10 5 1];
dbthreads = [30 25 20 15 10 5 1];
clients = [200 175 150 125 100 75 50 25 1];
for i = 2:length(threads)
    threadCount = threads(i);
    dbThreadCount = dbthreads(i);
    
    throughput = zeros(length(clients),591);
    
    for c = 1:length(clients);
        clientCount = clients(c);
        data = csvread(strcat('test',num2str(i),'.',num2str(c),'_server0_test.txt'),0,1);
        throughput(c,:) = (data(10:600,1) + data(10:600,2))';
    end
    errorbar(clients,mean(throughput,2)',std(throughput,0,2)');
    title(strcat('Throughput for threads=',num2str(threadCount)));
    xlabel Clients
    ylabel 'Throughput / Messages per second'
    hold on
    pause
end
legend('30 Threads', '25 Threads', '20 Threads', '15 Threads', '10 Threads',...
    '5 Threads', '1 Thread')
hold off

