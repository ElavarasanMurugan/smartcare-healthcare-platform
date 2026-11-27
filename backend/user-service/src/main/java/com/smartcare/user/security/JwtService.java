package com.smartcare.user.security;

import com.smartcare.user.config.JwtProperties;
import com.smartcare.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Service responsible for generating and validating HS256-signed JSON Web Tokens (JWT).
 */
@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32;

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET environment variable must be configured.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes (256 bits) for HS256.");
        }
        if (jwtProperties.getExpirationMs() <= 0) {
            throw new IllegalStateException("jwt.expiration-ms must be greater than 0.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT for an authenticated user without including sensitive credentials.
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(jwtProperties.getExpirationMs());

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getEffectiveRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parses and validates a JWT's signature and expiration timestamp.
     *
     * @throws JwtException if the token is expired, malformed, or has an invalid signature
     */
    public Claims parseAndValidateToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns {@code true} if the token has a valid signature and is not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            parseAndValidateToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.parseLong(parseAndValidateToken(token).getSubject());
    }

    public String extractEmail(String token) {
        return parseAndValidateToken(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseAndValidateToken(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }
}
