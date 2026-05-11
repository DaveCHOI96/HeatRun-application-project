package com.main.heatrun.global.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.main.heatrun.global.cache.LocationCacheService;
import com.main.heatrun.global.websocket.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWebSocketPublisher implements MessageListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final LocationCacheService locationCacheService;
    private final ObjectMapper objectMapper;

    // --- Redis -> WebSocket 브로드 캐스트 ---

    // Redis 채널ㅇ서 메시지 수신 -> WebSocket으로 전달
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String body = new String(message.getBody());
            LocationMessage locationMessage = objectMapper.readValue(body, LocationMessage.class);

            // 해당 크루 채널로 WebSocket 브로드캐스트
            String channel = new String(message.getChannel());
            UUID crewId =extractCrewId(channel);

            messagingTemplate.convertAndSend(
                    "/topic/crew/" + crewId + "/location", locationMessage
            );
        } catch (Exception e) {
            log.error("Redis Pub/Sub 메시지 처리 실패", e);
        }
    }

    // Redis 채널에 발행

    // 위치 메시지 Redis 채널에 발행
    public void publishLocation(UUID crewId, LocationMessage message) {
        try {
            String channel = getLocationChannel(crewId);
            String body = objectMapper.writeValueAsString(message);

            // Redis 채널에 발행 -> 모든 서버의 리스너가 수신
            redisTemplate.convertAndSend(channel, body);

            // 위치 캐시도 동시에 저장
            locationCacheService.saveLocation(message.userId(), message);
        } catch (Exception e) {
            log.error("위치 Redis 발행 실패: crewId={}", crewId, e);
        }
    }

    // 크루 채널 구독 등록
    public void subscribeCrew(UUID crewId) {
        listenerContainer.addMessageListener(
                this, new ChannelTopic(getLocationChannel(crewId))
        );
        log.info("Redis 채널 구독: crewId={}", crewId);
    }

    // 크루 채널 구독 해제
    public void unsubscribeCrew(UUID crewId) {
        listenerContainer.removeMessageListener(
                this, new ChannelTopic(getLocationChannel(crewId))
        );
        log.info("Redis 채널 구독 해제: crewId={}", crewId);
    }

    private String getLocationChannel(UUID crewId) {
        return "channel:crew:" + crewId + ":location";
    }
    private UUID extractCrewId(String channel) {
        // "channel:crew:{crewId}:location" -> crewId 추출
        String[] parts = channel.split(":");
        return UUID.fromString(parts[2]);
    }
}
