package com.example.mall.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SecurityStartupValidator implements ApplicationRunner {
    private static final Set<String> UNSAFE_PROD_JWT_SECRETS = Set.of(
            "mall-course-demo-secret-key-change-me-2026",
            "mall-demo-jwt-secret-at-least-32-bytes-2026"
    );

    private final Environment environment;

    @Value("${mall.jwt.secret}")
    private String jwtSecret;

    @Override
    public void run(ApplicationArguments args) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 长度至少需要 32 字节");
        }
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prod && (UNSAFE_PROD_JWT_SECRETS.contains(jwtSecret) || jwtSecret.toLowerCase().contains("change-me"))) {
            throw new IllegalStateException("生产环境必须配置非默认 JWT_SECRET");
        }
    }
}
