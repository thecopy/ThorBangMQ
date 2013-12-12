
clientPush = csvread('500k_peek.log');
clientPush = clientPush(:,2);
serverPush = csvread('peek.log');
serverPush = serverPush(:,5)./serverPush(:,4)./1000000;
diffs = zeros(1,length(serverPush)-2);
for p=2:length(serverPush)-3
   diffs(p) = mean(clientPush((p-1)*104+1:p*104))-serverPush(p);
   if(diffs(p) < 0)
       a = clientPush(43435435,1);
   end
end