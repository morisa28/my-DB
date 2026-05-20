package com.example.mall.security;

import com.example.mall.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LoginAttemptLimiter {
    private static final int MAX_FAILURES = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void assertAllowed(String username) {
        String key = key(username);
        Attempt attempt = attempts.get(key);
        if (attempt == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (attempt.lockedUntil() != null && now.isBefore(attempt.lockedUntil())) {
            throw new BusinessException(429, "登录失败次数过多，请稍后再试");
        }
        if (attempt.lastFailure().plus(FAILURE_WINDOW).isBefore(now)) {
            attempts.remove(key, attempt);
        }
    }

    public void recordFailure(String username) {
        String key = key(username);
        LocalDateTime now = LocalDateTime.now();
        attempts.compute(key, (ignored, previous) -> {
            Attempt current = previous;
            if (current == null || current.lastFailure().plus(FAILURE_WINDOW).isBefore(now)) {
                current = new Attempt(0, now, null);
            }
            int failures = current.failures() + 1;
            LocalDateTime lockedUntil = failures >= MAX_FAILURES ? now.plus(LOCK_DURATION) : current.lockedUntil();
            return new Attempt(failures, now, lockedUntil);
        });
    }

    public void recordSuccess(String username) {
        attempts.remove(key(username));
    }

    private String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private record Attempt(int failures, LocalDateTime lastFailure, LocalDateTime lockedUntil) {
    }
}
