package com.smartcare.user.security;

import com.smartcare.user.config.JwtProperties;
import com.smartcare.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String TEST_SECRET = "test-only-jwt-secret-key-with-at-least-32-bytes!";
    private static final String OTHER_SECRET = "different-secret-key-that-is-also-at-least-32-bytes!";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(TEST_SECRET);
        properties.setExpirationMs(3_600_000L);
        jwtService = new JwtService(properties);

        testUser = new User("Arun Kumar", "arun.phase3@example.com", "+91-9876543210", "$2a$10$hashedPasswordValue");
        testUser.setId(42L);
    }

    @Test
    void generateToken_validUser_createsSignedJwtWithExpectedClaimsAndNoCredentials() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));

        Claims claims = jwtService.parseAndValidateToken(token);
        assertEquals("42", claims.getSubject());
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("arun.phase3@example.com", jwtService.extractEmail(token));
        assertEquals("PATIENT", jwtService.extractRole(token));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));

        // Password and password hash must never be in JWT claims
        assertNull(claims.get("password"));
        assertNull(claims.get("passwordHash"));
        assertFalse(token.contains("hashedPasswordValue"));
    }

    @Test
    void parseAndValidateToken_expiredJwt_isRejected() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(120);
        String expiredToken = Jwts.builder()
                .subject("42")
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.isTokenValid(expiredToken));
        assertThrows(ExpiredJwtException.class, () -> jwtService.parseAndValidateToken(expiredToken));
    }

    @Test
    void parseAndValidateToken_invalidSignature_isRejected() {
        SecretKey otherKey = Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        String forgedToken = Jwts.builder()
                .subject("42")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(otherKey, Jwts.SIG.HS256)
                .compact();

        assertFalse(jwtService.isTokenValid(forgedToken));
        assertThrows(JwtException.class, () -> jwtService.parseAndValidateToken(forgedToken));
    }

    @Test
    void parseAndValidateToken_malformedJwt_isRejected() {
        String malformedToken = "not.a.valid.jwt.token";

        assertFalse(jwtService.isTokenValid(malformedToken));
        assertThrows(JwtException.class, () -> jwtService.parseAndValidateToken(malformedToken));
    }
}
