function [E_r, E_n, E_q] = MMm_K(arrivalRate, serviceRate, m, K)

    p = arrivalRate/(m*serviceRate);
    
    %% Calculate p0
    p0 = 1;
    for n=1:m-1
        p0 = p0 + (m*p)^n * nchoosek(K,n);
    end
    
    for n=m:K
        p0 = p0 + p^n*nchoosek(K,n) * factorial(n) / factorial(m) * m^m;
    end
    
    p0 = 1/p0;
    
    %% Calculate E[n]
    E_n = 0;
    for n=0:K
        E_n = E_n + n*p_n_bd(n,m,p,K,p0);
    end

    %% Calculate lambda_avg
    lambda_avg = arrivalRate*(K-E_n);
    
    %% Calculate E[r]
    E_r = E_n/lambda_avg;
    
    %% Calculate E[q]
    E_q = -1;
end