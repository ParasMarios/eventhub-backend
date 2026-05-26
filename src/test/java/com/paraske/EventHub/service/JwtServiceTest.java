package com.paraske.EventHub.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Key;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String SECRET_STRING = "your-256-bit-secret-your-256-bit-secret-your-256-bit-secret";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    @Test
    void generateToken_shouldGenerateValidToken() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        assertNotNull(token);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(username, claims.getSubject());
    }
}