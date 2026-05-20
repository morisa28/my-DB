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
    private static final Set<String> UNSAFE_PROD_DB_PASSWORDS = Set.of(
            "123456",
            "mall_demo_root_2026",
            "change-me-root-password-at-least-16-chars",
            "change-me-app-password-at-least-16-chars"
    );

    private final Environment environment;

    @Value("${mall.jwt.secret}")
    private String jwtSecret;

    @Value("${mall.cors.allowed-origins:*}")
    private String corsAllowedOrigins;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Override
    public void run(ApplicationArguments args) {
        if (jwtSecret == null || jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 长度至少需要 32 字节");
        }
        boolean prod = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (prod && (UNSAFE_PROD_JWT_SECRETS.contains(jwtSecret) || jwtSecret.toLowerCase().contains("change-me"))) {
            throw new IllegalStateException("生产环境必须配置非默认 JWT_SECRET");
        }
        if (prod && (corsAllowedOrigins == null || corsAllowedOrigins.isBlank() || corsAllowedOrigins.contains("*"))) {
            throw new IllegalStateException("生产环境必须配置明确的 CORS_ALLOWED_ORIGINS");
        }
        if (prod && "root".equalsIgnoreCase(datasourceUsername)) {
            throw new IllegalStateException("生产环境应用连接数据库禁止使用 root 账号");
        }
        if (prod && (datasourcePassword == null
                || datasourcePassword.getBytes(StandardCharsets.UTF_8).length < 12
                || UNSAFE_PROD_DB_PASSWORDS.contains(datasourcePassword)
                || datasourcePassword.toLowerCase().contains("change-me"))) {
            throw new IllegalStateException("生产环境必须配置非默认数据库密码");
        }
    }
}
