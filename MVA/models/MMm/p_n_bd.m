function [ x ] = p_n_bd(n, m, p, K, p0)
if(n > 0)
    if(n<m)
        x = p0 * (m*p)^n * nchoosek(K,n);
    else
        x = p0 * p^n * nchoosek(K,n) * factorial(n)/factorial(m) * m^m;
    end
else
    x = p0;
end

