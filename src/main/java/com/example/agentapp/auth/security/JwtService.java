package com.example.agentapp.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    /**
     * SECRET KEY
     *
     * GÜVENLİK:
     * - application.properties'den okunur
     * - Minimum 256 bits (32 karakter)
     * - Production'da MUTLAKA değiştirilmeli
     * - Environment variable olarak saklanmalı
     *
     * ÖRNEK (application.properties):
     * jwt.secret=MyVeryLongSecretKeyThatMustBeAtLeast256BitsLongForHS512Algorithm
     *
     * PRODUCTION:
     * export JWT_SECRET="..."
     * jwt.secret=${JWT_SECRET}
     */
    @Value("${jwt.secret}")
    private String secretKeyString;

    /**
     * TOKEN EXPIRATION (milisaniye)
     *
     * Default: 86400000 ms = 24 saat
     *
     * application.properties:
     * jwt.expiration=86400000
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * REFRESH TOKEN EXPIRATION
     *
     * Default: 604800000 ms = 7 gün
     */
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * SECRET KEY'i SecretKey nesnesine çevir
     *
     * HS512 için minimum 512 bits (64 bytes) gerekir
     *
     * GÜVENLİK: Key HMAC-SHA512 için oluşturulur
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /**
     * ACCESS TOKEN OLUŞTUR
     *
     * NASIL ÇALIŞIR:
     * 1. Username'i al
     * 2. Claims'leri oluştur (sub, iat, exp, userId)
     * 3. HS512 ile imzala
     * 4. Compact string olarak döndür
     *
     * TOKEN YAPISI:
     * {
     *   "sub": "johndoe",       // username
     *   "userId": 123,          // custom claim
     *   "iat": 1640995200,      // issued at
     *   "exp": 1641081600       // expiration (24 saat sonra)
     * }
     *
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                // CLAIMS: Token içindeki bilgiler
                .setClaims(claims)

                // SUBJECT: Genelde username
                .setSubject(username)

                // ISSUED AT: Token oluşturulma zamanı
                .setIssuedAt(new Date())

                // EXPIRATION: Token'ın son kullanma tarihi
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))

                // SIGNATURE: HS512 ile imzala
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)

                // STRING'e çevir
                .compact();
    }

    /**
     * REFRESH TOKEN OLUŞTUR
     *
     * Access token ile aynı ama:
     * - Daha uzun ömürlü (7 gün)
     * - Database'de saklanır
     */
    public String generateRefreshToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token); // imza doğrulama

            // 1. Username eşleşiyor mu?
            // 2. Token expire olmamış mı?
            return (extractedUsername.equals(username) && !isTokenExpired(token));

        } catch (ExpiredJwtException e) {
            // Token expire olmuş
            throw e;
        } catch (MalformedJwtException e) {
            // Token formatı yanlış
            throw new RuntimeException("Geçersiz token formatı", e);
        } catch (Exception e) {
            // Diğer hatalar
            throw new RuntimeException("Token doğrulama hatası", e);
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // ====================================================================
    // TOKEN'DAN BİLGİ ÇIKARMA (EXTRACTION)
    // ====================================================================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                // SECRET KEY ile doğrula
                .setSigningKey(getSigningKey())
                .build()
                // Token'ı parse et
                .parseClaimsJws(token)
                // Claims'leri al
                .getBody();
    }

    // ====================================================================
    // GETTER METHODS (Configuration için)
    // ====================================================================


    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    public Long getJwtExpirationInSeconds() {
        return jwtExpiration / 1000;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}