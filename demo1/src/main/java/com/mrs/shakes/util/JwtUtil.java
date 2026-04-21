package com.mrs.shakes.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}") // application.properties 에 설정된 비밀키
    private String secretKey;

    // 1. 모든 Claims 추출
    public Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .setSigningKey(key)
                //.build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 2. 특정 Claim 추출 (예: userId, nickname 등)
    public Object getClaim(String token, String claimName) {
        Claims claims = extractAllClaims(token);
        return claims.get(claimName);
    }

    // 3. Subject(보통 userId로 설정한 값) 추출
    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }
}