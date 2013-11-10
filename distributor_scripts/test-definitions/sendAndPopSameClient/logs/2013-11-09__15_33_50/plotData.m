pop = csvread('pop.log', 0,1);
push = csvread('push.log', 0,1);
peek = csvread('peek.log',0,1);

oneWay = input('One Way clients: ');
twoWay = input('Two Way clients: ');

workerThreads = input('Worker threads: ');
dbThreads = input('DB Threads: ');

testlog_data = csvread('test_log.txt',0,1);
throughPut = testlog_data(:,1) + testlog_data(:,2);

p.peek = peek;
p.push = push;
p.pop = pop;
p.config.workerthreads = workerThreads;
p.config.dbthreads = dbThreads;
p.config.clients.oneway = oneWay;
p.config.clients.twoway = twoWay;

p.throughut = throughPut;
