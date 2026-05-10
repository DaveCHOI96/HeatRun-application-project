package com.main.heatrun.global.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrewCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 크루 점령률 캐시 TTL - 5분
    private static final long CREW_STATS_TTL_MINUTES = 5;

    // --- 크루 점령률 캐시 ---

    // 크루 점령률 저장
    public void saveCrewStats(UUID crewId, CrewStatsDto stats) {
        try {
            String key = getCrewStatsKey(crewId);
            String value = objectMapper.writeValueAsString(stats);

            redisTemplate.opsForValue().set(
                    key, value, CREW_STATS_TTL_MINUTES, TimeUnit.MINUTES
            );
            log.debug("크루 점령률 캐시 저장: crewId={}", crewId);
        } catch (Exception e) {
            log.error("크루 점령률 캐시 저장 실패: crewId={}", crewId, e);
        }
    }

    // 크루 점령률 조회
    public CrewStatsDto getCrewStats(UUID crewId) {
        try {
            String key = getCrewStatsKey(crewId);
            String value = redisTemplate.opsForValue().get(key);

            if (value == null) return null;

            return objectMapper.readValue(value, CrewStatsDto.class);
        } catch (Exception e) {
            log.error("크루 점령률 캐시 조회 실패: crewId={}", crewId, e);
            return null;
        }
    }

    // 크루 캐시 무효화 (멤버 변경 시)
    public void invalidateCrewStats(UUID crewId) {
        redisTemplate.delete(getCrewStatsKey(crewId));
        log.info("크루 점령률 캐시 무효과: crewId={}", crewId);
    }

    // 러닝 중인 크루원 수 저장
    public void saveActiveRunnerCount(UUID crewId, int count) {
        redisTemplate.opsForValue().set(
                getActiveRunnerKey(crewId),
                String.valueOf(count),
                CREW_STATS_TTL_MINUTES, TimeUnit.MINUTES
        );
    }

    // 러닝 중인 크루원 수 조회
    public Integer getActiveRunnerCount(UUID crewId) {
        String value = redisTemplate.opsForValue().get(getActiveRunnerKey(crewId));
        return value != null ? Integer.parseInt(value) : null;
    }

    private String getCrewStatsKey(UUID crewId) {
        return "crew:stats:" + crewId;
    }
    private String getActiveRunnerKey(UUID crewId) {
        return "crew:runners:" + crewId;
    }

    public record CrewStatsDto(
            UUID crewId,
            long totalTiles, // 크루 총 점령 타일
            long memberCount, // 크루 멤버 수
            Integer activeRunners // 현재 러닝 중인 멤버
    ) {}
}
