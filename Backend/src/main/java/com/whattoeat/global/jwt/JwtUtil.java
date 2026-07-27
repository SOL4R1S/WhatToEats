package com.whattoeat.global.jwt;

import com.whattoeat.domain.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private SecretKey key;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("loginId", user.getLoginId())
                .claim("role", user.getRole().name())
                .claim("tokenType", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ accessExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("tokenType", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+ refreshExpiration))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
         return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
    }
    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
    public long getRemainingExpiration(String token) {
        Date expiration = parseToken(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
