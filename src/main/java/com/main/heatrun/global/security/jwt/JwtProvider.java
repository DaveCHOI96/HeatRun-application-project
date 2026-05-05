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
        return Keys.hmacShaKeyFor( // 문자열 secret을 HMAC-SHA 알고리즘용 키로 변환
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // 토큰 발급

    //엑세스 토큰
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString()) // 유저 식별자
                // claim = 커스텀 데이터 추가 (서버에서 DB조회 없이 바로 사용)
                .claim("email", user.getEmail())
                .claim("nickname", user.getNickname())
                .claim("provider", user.getProvider().name())
                .issuedAt(now)   // 발급 시간
                .expiration(expiry)  // 만료 시간
                .signWith(getSigningKey())  // 서명 (이것이 없으면 누구나 토큰 위조 가능)
                .compact();  // 문자열로 직렬화

        //왜 email, nickname을 claim에 넣나?
        //→ API 요청마다 DB 조회 없이 토큰에서 바로 꺼내 쓸 수 있음
        //→ 성능 최적화
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
                getRefreshKey(user.getId()),  // "refresh:{userId}"
                refreshToken,
                jwtProperties.getRefreshTokenExpiration(),
                TimeUnit.MILLISECONDS   // TTL 단위
        );
        return refreshToken;
        //Sliding Session
        //→ 액세스 토큰 재발급마다 이 함수 호출
        //→ TTL이 14일로 리셋
        //→ 계속 쓰는 한 로그아웃 안됨
    }

    // 토큰 검증

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey()) // 서명이 우리 서버가 발급한 게 맞는지 확인
                    .build()
                    .parseSignedClaims(token); // 만료시간 체크
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
        // Redis에 "blacklist:{userId}" 키가 있는지 확인
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
