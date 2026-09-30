package br.org.edu.ifrn.lojacarro.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    @Value("${ratelimit.max-requests:20}")
    private int maxRequests;

    @Value("${ratelimit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, RequestWindow> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = resolveClientKey(request);
        long nowMillis = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        RequestWindow window = requestCounts.compute(clientKey, (key, existing) -> {
            if (existing == null || nowMillis - existing.windowStartMillis >= windowMillis) {
                return new RequestWindow(nowMillis, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int total = window.count.get();
        if (total > maxRequests) {
            if (total == maxRequests + 1) {
                log.warn("Limite de {} requisicoes em {}s estourado por {}. Bloqueando ate a janela virar",
                        maxRequests, windowSeconds, clientKey);
            } else {
                log.debug("Requisicao {} de {} bloqueada pelo rate limit", total, clientKey);
            }
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests - rate limit exceeded\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RequestWindow {
        final long windowStartMillis;
        final AtomicInteger count;

        RequestWindow(long windowStartMillis, AtomicInteger count) {
            this.windowStartMillis = windowStartMillis;
            this.count = count;
        }
    }
}
