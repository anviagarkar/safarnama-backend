package com.safarnama.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// The forgot/reset-password flow uses a 6-digit numeric code (~900,000
// possible values) with a 1-hour expiry. Without a limit on attempts, that's
// brute-forceable well within the hour by anyone who knows a user's email.
// This filter caps attempts per client IP rather than per-token, since a
// wrong guess doesn't correspond to any stored row to attach a counter to.
//
// In-memory and per-instance — fine for a single Render web service. If this
// ever runs on multiple instances behind a load balancer, this would need to
// move to a shared store (e.g. the database or Redis) to stay effective.
@Component
public class ResetPasswordRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MILLIS = 15 * 60 * 1000; // 15 minutes

    private final ConcurrentHashMap<String, List<Instant>> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        boolean isGuardedPath = path.equals("/auth/forgot-password") || path.equals("/auth/reset-password");

        if (!isGuardedPath) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = clientIp(request);
        List<Instant> attempts = attemptsByIp.computeIfAbsent(clientIp, key -> new CopyOnWriteArrayList<>());

        Instant cutoff = Instant.now().minusMillis(WINDOW_MILLIS);
        attempts.removeIf(timestamp -> timestamp.isBefore(cutoff));

        if (attempts.size() >= MAX_ATTEMPTS) {
            response.setStatus(429);
            response.setContentType("text/plain");
            response.getWriter().write("Too many attempts. Please try again later.");
            return;
        }

        attempts.add(Instant.now());
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        // Render sits behind a proxy, so prefer the forwarded-for header
        // when present; fall back to the direct remote address otherwise.
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}