package com.main.heatrun.global.security.jwt;

import com.main.heatrun.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    // 키 생성
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // 토큰 발급

    //엑세스 토큰
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .claim("provider", user.getProvider().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // 리프레시 토큰 발급 + Redis 저장 (Sliding)
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        String refreshToken = Jwts.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();

        // Redis에 저장 - 접속마다 TTL 리셋 (Sliding Session)
        redisTemplate.opsForValue().set(
                getRefreshKey(user.getId()),
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    // 토큰 검증

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있음: {}", e.getMessage());
        }
        return false;
    }

    //  블랙리스트 체크 (강제 로그아웃 / 계정 정지)
    public boolean isBlacklisted(UUID userId) {
        Boolean result = redisTemplate.hasKey(getBlacklistKey(userId));
        return result != null && result;
    }

    // Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).getSubject());
    }

    // refreshToken 관련

    // Redis에서 리프레시 토큰 조회
    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(getRefreshKey(userId));
    }

    // 리프레시 토큰 삭제 (로그아웃)
    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(getRefreshKey(userId));
    }

    // 블랙리스트 등록(강제 로그아웃 / 계정 정지)
    public void addBlacklist(UUID userId) {
        redisTemplate.opsForValue().set(
                getBlacklistKey(userId),
                "banned",
                jwtProperties.getAccessTokenExpiration(), // 액세스 토큰 만료시간만큼
                TimeUnit.MILLISECONDS
        );
    }

    // Redis Key 네이밍
    private String getRefreshKey(UUID userId) {
        return "refresh:" + userId;
    }

    private String getBlacklistKey(UUID userId) {
        return "blacklist:" + userId;
    }
}
