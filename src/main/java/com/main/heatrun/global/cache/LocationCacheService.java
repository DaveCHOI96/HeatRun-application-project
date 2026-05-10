package com.main.heatrun.global.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.heatrun.global.websocket.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 위치 캐시 TTL - 러닝 최대 시간 6시간
    private static final long LOCATION_TTL_HOURS = 6;

    // ---- 위치 저장 ----

    // 유저 현재 위치 Redis 저장
    public void saveLocation(UUID userId, LocationMessage location) {
        try {
            String key = getLocationKey(userId);
            String value = objectMapper.writeValueAsString(location);

            // TTL 6시간 - 러닝 종료 시 자동 삭제
            redisTemplate.opsForValue().set(
                    key, value, LOCATION_TTL_HOURS, TimeUnit.HOURS
            );
        } catch (Exception e) {
            log.error("위치 캐시 저장 실패: userId={}", userId, e);
        }
    }

    // 유저 현재 위치 조회
    public LocationMessage getLocation(UUID userId) {
        try {
            String key = getLocationKey(userId);
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) return null;

            return objectMapper.readValue(value, LocationMessage.class);
        } catch (Exception e) {
            log.error("위치 캐시 조회 실패 userId={}", userId, e);
            return null;
        }
    }

    // 크루 전체 위치 조회 (라이브 크루 맵)
    public List<LocationMessage> getCrewLocations(List<UUID> memberIds) {
        List<LocationMessage> locations = new ArrayList<>();

        memberIds.forEach(memberId -> {
            LocationMessage location = getLocation(memberId);
            // RUNNING 또는 PAUSED 상태인 멤버만 반환
            if (location != null && !"STOPPED".equals(location.status())) {
                locations.add(location);
            }
        });
        return locations;
    }

    // 위치 삭제 (러닝 종료 시)
    public void deleteLocation(UUID userId) {
        redisTemplate.delete(getLocationKey(userId));
        log.info("위치 캐시 삭제: userId={}", userId);
    }

    // --- Redis Key ---
    private String getLocationKey(UUID userId) {
        return "location:" + userId;
    }
}
