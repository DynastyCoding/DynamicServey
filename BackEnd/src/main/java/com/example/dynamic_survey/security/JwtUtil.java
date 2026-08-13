package com.example.dynamic_survey.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")     private String jwtSecret;
    @Value("${jwt.expiration}") private int jwtExpirationMs;

    // jwt.secret 是 Base64 編碼過的亂數，要先解碼還原成位元組
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getUsername())                 // 主題 = Email
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS512)            // 簽名
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    // 從 Token 取出帳號 (Email)
    public String getUserNameFromJwtToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    // 驗證 Token：帳號要對得上，且尚未過期，兩者都成立才算有效
    public boolean validateJwtToken(String token, String email) {
        String tokenEmail = getUserNameFromJwtToken(token);
        boolean expired = extractAllClaims(token).getExpiration().before(new Date());
        return tokenEmail.equals(email) && !expired;
    }
}