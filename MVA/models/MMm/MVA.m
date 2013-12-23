function [X_r, Q] = MVA(M,m,N,Z,V,S,u,T)
    Q = zeros(M,N);
    R = zeros(1,M);
    P = zeros(M,N+1); %+1 b/c [0,N], not [1,N]
    P(:,1) = ones(M,1); % at beginning 100% chance of having 0 clients
    
    X_r = zeros(1,N);
    
    for n=1:N
            R_sum = 0;
            for i=1:M
                        
                if T(i) == 1
                    % If > 1 servers:
                    q = min(m(i),n);
                    R(i) = S(i)*(1+Q(i))/q;
                    
                elseif T(i) == 2
                    R(i) = S(i);
                    
                elseif T(i) == 3
                    R(i) = 0;
                    for j=1:n
                        R(i) = R(i) + P(i,j) * j * u(i,j)/min(m(i),j);
                        Q(i,n) = Q(i,n) + j*P(i,j+1);
                    end
                    
                else
                    error('Unkown station type: %d', T(i));
                end

                R_sum = R_sum + R(i) * V(i);
            end

            X = n/(Z+R_sum);
            X_r(n) = X;
            for i=1:M
                if T(i) == 3
                    for j=n:-1:1
                        P(i,j+1) = X * u(i,j)/min(m(i),j) * P(i,j);
                        if P(i,j+1) < 0
                            error('P(%d,%d) = %2.5f < 0', i, j+1, P(i,j+1))
                        end
                    end
                    P(i,1) = 1;
                    for j=1:n
                        P(i,1) = P(i,1) - P(i,j+1);
                    end
                    if P(i,1) < 0
                        warning('P < 0 when n = %d, P=%2.20f', n, P(i,1))
                        P(i,1) = 0;
                    end
                    
                    if trapz(P(i,:) > 1)
                        error('TRAPS > 1, N = %d', n)
                    end
                    if sum(P(i,:) > 1)
                        warning('SUM > 1')
                    end
                else
                    Q(i) = X*V(i)*R(i);
                end
            end
            
            %plot(P(2,:));
            %pause(0.01);
    end
end