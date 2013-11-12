throughput = [];
thinkTime = [];
dataPoints = [[1 61]; [1 61]; [22 1222]; [25 1250]; [34 1210];[34 1210];[34 1210];[34 1210*2]];

servers = [1 2];
clients = [1 2 3 4 5 6 7; ...
           4 0 0 0 0 0 0];
for s = 1:length(servers)
    for c = 1:length(clients)
        server = servers(s);
        client = clients(s,c);
        if(client == 0)
            continue;
        end
        if(server > 1)
            data = [];
            for i=1:server;
                data2 = csvread(strcat(num2str(client),'clients',num2str(server),'-',...
                    num2str(i),'.log'),0,0, [dataPoints(c*s,1) 0 dataPoints(s*c,2) 6]);
                data = [data;data2];
            end
        else
            data = csvread(strcat(num2str(client),'clients',num2str(server),'.log'),0,0,...
                    [dataPoints(c,1) 0 dataPoints(c,2) 6]);
        end
        size(data)
    end
end