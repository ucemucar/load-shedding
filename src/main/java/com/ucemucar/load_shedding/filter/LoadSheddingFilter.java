package com.ucemucar.load_shedding.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoadSheddingFilter implements Filter {
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final int maxRequests = 100;
    private final long resetIntervalMillis = 10000;

    private long lastResetTime = System.currentTimeMillis();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // To simulate delay/load, we wait 10 seconds to reset request counter.
        // Be aware that this is just for testing and shouldn't be here
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastResetTime > resetIntervalMillis) {
            requestCount.set(0);
            lastResetTime = currentTime;
        }

        if (requestCount.incrementAndGet() > maxRequests) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;  // Limit reached, block new requests
        }

        chain.doFilter(request, response);

        // Below line also is commented out for testing. Normally we
        // should decrement request counter after response is served
        //requestCount.decrementAndGet();
    }

    @Override
    public void destroy() {}

}
