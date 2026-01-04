package com.smartcrops.jwt;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    // ✅ FIX: Use FIXED secret key (NOT random)
    private static final String SECRET =
            "smart-crop-system-secret-key-123456789";

    private static final Key key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    // 🔐 GENERATE TOKEN WITH ROLE (UNCHANGED)
    public static String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION_TIME)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 🔍 VALIDATE TOKEN & RETURN CLAIMS (UNCHANGED)
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =================================================
    // ✅ ADDED METHODS (NO EXISTING CODE CHANGED)
    // =================================================

    // 🔍 Extract username (email) from JWT
    public static String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    // 🔍 Extract role from JWT
    public static String extractRole(String token) {
        return validateToken(token).get("role", String.class);
    }
}
