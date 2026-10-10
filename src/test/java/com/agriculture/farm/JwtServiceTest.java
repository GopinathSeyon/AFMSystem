package com.agriculture.farm;

import com.agriculture.farm.auth.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private String secret;
    private JwtService service;
    private UserDetails user;

    private static String newSecret() {
        return Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded());
    }

    @BeforeEach
    void setUp() {
        secret = newSecret();
        service = new JwtService(secret, 60_000);
        user = User.withUsername("ravi@example.com").password("x").roles("FARMER").build();
    }

    @Test
    void createsAndValidatesToken() {
        String token = service.generateToken(user);
        assertEquals("ravi@example.com", service.extractEmail(token));
        assertTrue(service.isValid(token, user));
    }

    @Test
    void rejectsTokenForDifferentUser() {
        String token = service.generateToken(user);
        UserDetails other = User.withUsername("other@example.com").password("x").roles("FARMER").build();
        assertFalse(service.isValid(token, other));
    }

    @Test
    void rejectsExpiredToken() {
        JwtService expired = new JwtService(secret, -1_000);
        assertFalse(expired.isValid(expired.generateToken(user), user));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String foreign = new JwtService(newSecret(), 60_000).generateToken(user);
        assertFalse(service.isValid(foreign, user));
    }

    @Test
    void rejectsGarbage() {
        assertFalse(service.isValid("not-a-jwt", user));
    }
}
