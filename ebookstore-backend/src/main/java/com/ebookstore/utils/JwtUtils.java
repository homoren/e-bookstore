package com.ebookstore.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    // 密钥（至少32位）
    private static final String SECRET = "my-ebookstore-secret-key-2024-very-long-key";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7天

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // 生成Token
    public String generateToken(String username, Long userId, Integer role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 从Token获取用户名
    public String getUsernameFromToken(String token) {
        return parseToken(token).getBody().getSubject();
    }

    // 从Token获取用户ID
    public Long getUserIdFromToken(String token) {
        return parseToken(token).getBody().get("userId", Long.class);
    }

    // 从Token获取角色
    public Integer getRoleFromToken(String token) {
        return parseToken(token).getBody().get("role", Integer.class);
    }

    // 验证Token是否有效
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}