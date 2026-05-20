package com.example.mall.utils;

import com.example.mall.exception.BusinessException;
import com.example.mall.security.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${mall.jwt.secret}")
    private String secret;

    @Value("${mall.jwt.expiration-minutes}")
    private Long expirationMinutes;

    public String generateToken(Long userId, String username, Integer role, Integer sessionVersion) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMinutes * 60 * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim("sessionVersion", sessionVersion == null ? 0 : sessionVersion)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key())
                .compact();
    }

    public LoginUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Long userId = Long.valueOf(claims.getSubject());
            String username = claims.get("username", String.class);
            Integer role = claims.get("role", Integer.class);
            Integer sessionVersion = claims.get("sessionVersion", Integer.class);
            return new LoginUser(userId, username, role, sessionVersion == null ? 0 : sessionVersion);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(401, "Token 无效或已过期");
        }
    }

    private SecretKey key() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new BusinessException(500, "JWT 密钥长度至少需要 32 字节");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
