package com.diego.interview.infraestructure.security;

import com.diego.interview.domain.model.User;
import com.diego.interview.domain.port.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

public class JwtTokenProviderPort implements TokenProviderPort {

    private final Key key;
    private final long expirationInSeconds;

    public JwtTokenProviderPort(String secret, long expirationInSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationInSeconds = expirationInSeconds;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationInSeconds);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setId(user.getId() != null ? user.getId().toString() : null)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    @Override
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
