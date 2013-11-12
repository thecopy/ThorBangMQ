sizes = [10 50000 500000 3300000 5000000]';
sizes = sizes./1000; % kB
push = zeros(5, 5000);
pop = zeros(5, 5000);
peek = zeros(5, 5000);

push(1,:) = csvread('repeat_push_20.log',0,1)';
push(2,:) = csvread('repeat_push_50k.log',0,1)';
push(3,:) = csvread('repeat_push_500k.log',0,1)';
push(4,:) = csvread('repeat_push_3.3M.log',0,1)';
push(5,:) = csvread('repeat_push_5M.log',0,1)';

pop(1,:) = csvread('repeat_pop_20.log',0,1)';
pop(2,:) = csvread('repeat_pop_50k.log',0,1)';
pop(3,:) = csvread('repeat_pop_500k.log',0,1)';
pop(4,:) = csvread('repeat_pop_3.3M.log',0,1)';
pop(5,:) = csvread('repeat_pop_5M.log',0,1)';

peek(1,:) = csvread('repeat_peek_20.log',0,1)';
peek(2,:) = csvread('repeat_peek_50k.log',0,1)';
peek(3,:) = csvread('repeat_peek_500k.log',0,1)';
peek(4,:) = csvread('repeat_peek_3.3M.log',0,1)';
peek(5,:) = csvread('repeat_peek_5M.log',0,1)';


subplot(2,1,1)
hold off
h = errorbar(sizes, mean(push,2), std(push,0,2), 'r*-');
hold on
errorbar(sizes, mean(pop,2), std(pop,0,2), 'b+-');
errorbar(sizes, mean(peek,2), std(peek,0,2), 'go-');
hold off
%set(get(h,'Parent'), 'XScale', 'log')
legend Push Pop Peek
legend('Location','NorthWest')
grid on
xlabel 'Message Size / kB'
ylabel 'Response Time / ms'


subplot(2,1,2)
hold off
hold on
errorbar(sizes, mean(pop,2), std(pop,0,2), 'b+-');
errorbar(sizes, mean(peek,2), std(peek,0,2), 'go-');
hold off
legend Pop Peek
legend('Location','NorthWest')
grid on
xlabel 'Message Size / kB'
ylabel 'Response Time / ms'