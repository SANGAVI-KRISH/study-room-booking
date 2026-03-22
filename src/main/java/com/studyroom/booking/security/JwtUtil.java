package com.studyroom.booking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey12345";
    private static final long JWT_EXPIRATION_MS = 24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required to generate token");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION_MS);

        return Jwts.builder()
                .subject(email.trim())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration == null || expiration.before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            Claims claims = extractAllClaims(token.trim());
            String email = claims.getSubject();
            Date expiration = claims.getExpiration();

            return email != null
                    && !email.isBlank()
                    && expiration != null
                    && expiration.after(new Date());
        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("JWT token is required");
        }

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token.trim())
                .getPayload();
    }
}