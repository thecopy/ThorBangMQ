s = 0.03*ones(1,30); % service time
V = [1 zeros(1,29)]; % visit count array
N = 64; % num. of customers
Z = 0; % think time
K = 30; % num middleware

%output: average residence times R,
%        total residence time R_total,
%        average queue lengths Q,
%        system throughput X


% Intialize queueing centers to empty.
Q = zeros(K,1);

% Loop for 1 to N customers in the system.
for n = 1:N
  % Calculate residence times using queue lengths
  % for n-1 customers in the system.
  for k = 1:K
    R(k) = s(k) * (1 + Q(k));
  end
  % Total residence time at all queueing centers,
  % taking into account the average number of visits
  % made to each center.
  R_total = sum(V .* R);
  % System throughput, using R_total and think time.
  X = n / (R_total + Z);
  
  % Calculate new queue lengths for n customers
  % in the system.
  for k = 1:K
    Q(k) = X * V(k) * R(k);
  end
end
