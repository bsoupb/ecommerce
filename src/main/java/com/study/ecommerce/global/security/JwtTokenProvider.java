package com.study.ecommerce.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

// username 가져오기, authentication 가져오기, 
// 해당 토큰이 사용가능한 토큰인지, 토큰 가져오기(requestHeader),
// 토큰 만료여부, 토큰 확인

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.token-validity-in-milliseconds}")
    private long tokenValidityMilliSeconds;

    private Key key;

    private final UserDetailsService userDetailsService;

    // @PostConstruct: 빈이 생성된 후, 의존성 주입이 완료된 다음 자동으로 실행되도록 지정
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

    // 토큰 가져오기
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검사
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    // JWT vs JWS
    // JWT: 토큰 포맷
    // JWS: JWT를 서명(Signature)으로 보호

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
