package com.agriculture.farm.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    public static final String DEV_SECRET = "ZGV2LW9ubHktc2VjcmV0LWtleS1jaGFuZ2UtbWUtYWdyaWN1bHR1cmUtZmFybS1zeXN0ZW0tMDEyMzQ1Njc4OQ==";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
        if (DEV_SECRET.equals(secret)) {
            log.warn("Using the built-in DEVELOPMENT JWT secret. Set JWT_SECRET before deploying.");
        }
    }

    public String generateToken(UserDetails user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) { return parse(token).getSubject(); }

    public boolean isValid(String token, UserDetails user) {
        try {
            Claims c = parse(token);
            return user.getUsername().equals(c.getSubject()) && c.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
