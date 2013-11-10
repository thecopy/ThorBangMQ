threads = [250 200 100 50];
dbthreads = [35 35 35 35];
clients = [200 175 150 100 25];

for i = 1:length(threads)
    threadCount = threads(i);
    dbThreadCount = dbthreads(i);
    
    throughput = zeros(length(clients),581);
    
    for c = 1:length(clients);
        clientCount = clients(c);
        data = csvread(strcat('test',num2str(i),'.',num2str(c),'_server0_test.txt'),0,1);
        throughput(c,:) = (data(20:600,1) + data(20:600,2))'./2;
    end
    errorbar(clients,mean(throughput,2)',std(throughput,0,2)', 'color', [rand rand rand]);
    title(strcat('Throughput for threads=',num2str(threadCount)));
    xlabel Clients
    ylabel 'Throughput / Messages per second'
    hold on
    pause
end
legend('250 Threads', '200 Threads', '100 Threads', '50 Threads')
legend('Location', 'East')
hold off

