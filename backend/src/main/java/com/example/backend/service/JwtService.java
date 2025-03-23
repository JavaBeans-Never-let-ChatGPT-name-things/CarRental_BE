package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {
    @Value("${secret.secret-key}")
    private String secretKey;
    @Value("${secret.access-token-expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    @Value("${secret.refresh-token-expiration}")
    private long REFRESH_TOKEN_EXPIRATION;
    public String generateAccessToken (AccountEntity account)
    {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return Jwts.builder()
                .subject(account.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .claims().add(claims)
                .and()
                .signWith(getKey())
                .compact();
    }
    public String generateRefreshToken(AccountEntity account) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return Jwts.builder()
                .subject(account.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claims().add(claims)
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser().
                verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            String type = claims.get("type", String.class);
            return isTokenNotExpired(token) && type.equals("access");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("{}", e.getMessage());
            return false;
        }
    }
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null) {
                return false;
            }
            String type = claims.get("type", String.class);
            return (isTokenNotExpired(token)) && type.equals("refresh");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("{}", e.getMessage());
            return false;
        }
    }
    private boolean isTokenNotExpired(String token){
        return !extractExpiration(token).before(new Date());
    }
    private Date extractExpiration(String token){
            return extractClaim(token, Claims::getExpiration);
    }
}
