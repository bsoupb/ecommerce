package com.study.ecommerce.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token-validity-in-milliseconds}")
    private long tokenValidityMilliSeconds;

    private Key key;

    private final UserDetailsService userDetailsService;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityMilliSeconds);

        return Jwts.builder()
                .setClaims(claims)      // 클레임을 JWT에 삽입
                .setIssuedAt(now)       // 토큰 발급 시간
                .setExpiration(validity)    // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)    // HMAC SHA-256 알고리즘으로 서명 (변조 방지)
                .compact();     // JWT 문자열로 직렬화하여 반환
    }
}
