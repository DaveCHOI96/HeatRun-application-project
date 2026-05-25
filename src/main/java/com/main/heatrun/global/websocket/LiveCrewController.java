package com.main.heatrun.global.websocket;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.CrewMemberRepository;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.cache.LocationCacheService;
import com.main.heatrun.global.exception.BusinessException;
import com.main.heatrun.global.websocket.dto.CheerMessage;
import com.main.heatrun.global.websocket.dto.LocationMessage;
import com.main.heatrun.global.websocket.dto.LocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveCrewController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final RedisWebSocketPublisher redisPublisher;
    private final LocationCacheService locationCacheService;

    // 실시간 위치 전송
    // 클라이언트 -> /app/location/{crewId}
    // 브로드캐스트 -> /topic/crew/{crewId}/location

    @MessageMapping("/location/{crewId}")
    public void sendLocation(
            @DestinationVariable UUID crewId, @Payload LocationRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        //WebSocket 세션에서 userId 추출
        UUID userId = extractUserId(headerAccessor);
        User user = findUser(userId);

        // 크루 멤버인지 확인
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            log.warn("크루 비멤버 위치 전송 시도: userId={}, crewId={}", userId, crewId);
            return;
        }

        // 위치 메시지 생성
        LocationMessage message = new LocationMessage(
                userId,
                user.getNickname(),
                user.getProfileImageUrl(),
                request.latitude(),
                request.longitude(),
                request.speed(),
                request.status(),
                LocalDateTime.now().toString() // ISO 8601 문자열
        );

        // Redis Pub/Sub 으로 발행 -> 모든 서버로 브로드캐스트
        // (단일 서버면 직접 WebSocket 브로드캐스트와 동일)
        redisPublisher.publishLocation(crewId, message);

        // 크루 전체에게 브로드캐스트
//        messagingTemplate.convertAndSend("/topic/crew/" + crewId + "/location", message);
//        log.debug("위치 전송: userId={}, crewId={}, lat={}, lng={}",
//                userId, crewId, request.latitude(), request.longitude());
    }

    // 크루 현재 위치 목록 조회
    // 앱 재접속 시 현재 러닝 중인 크루원 위치 복원
    @MessageMapping("/crew/{crewId}/locations")
    public void getCrewLocations(
            @DestinationVariable UUID crewId, SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = extractUserId(headerAccessor);

        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            return;
        }

        // 크루 멤버 ID 목록 조회
        List<UUID> memberIds = crewMemberRepository.findUserIdsByCrewId(crewId);

        // Redis에서 현재 위치 목록 조회
        List<LocationMessage> locations = locationCacheService.getCrewLocations(memberIds);

        // 요청한 유저에게만 전송
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/crew-locations",
                locations
        );
    }

    // ---- 실시간 응원 전송 ----
    // 클라이언트 -> /app/cheer/{crewId}/{receiverId}
    // 개인 메시지 -> /user/{receiverId}/queue/cheer
    @MessageMapping("/cheer/{crewId}/{receiverId}")
    public void sendCheer(
            @DestinationVariable UUID crewId, @DestinationVariable UUID receiverId,
            SimpMessageHeaderAccessor headerAccessor) {
        UUID senderId = extractUserId(headerAccessor);
        User sender = findUser(senderId);

        // 자기 자신에게 응원 불가
        if (senderId.equals(receiverId)) {
            return;
        }

        // 크루 멤버인지 확인
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, senderId) ||
               !crewMemberRepository.existsByCrewIdAndUserId(crewId, receiverId)) {
            return;
        }

        CheerMessage message = new CheerMessage(
                senderId,
                sender.getNickname(),
                receiverId,
                null, // CheerType은 REST API에서 처리
                LocalDateTime.now().toString()
        );

        // 수신자에게만 개인 메시지 전송
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/cheer",
                message
        );
        log.info("응원 WebSocket 전송: from={}, to={}", senderId, receiverId);
    }

    // 러닝 종료 알림
    // 클라이언트 → /app/stop/{crewId}
    // 브로드캐스트 → /topic/crew/{crewId}/location (STOPPED 상태로)
    @MessageMapping("/stop/{crewId}")
    public void stopSharing(@DestinationVariable UUID crewId, SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = extractUserId(headerAccessor);
        User user = findUser(userId);

        // Redis 위치 캐시 삭제
        locationCacheService.deleteLocation(userId);

        //  STOPPED 상태로 마지막 메시지 전송
        LocationMessage message = new LocationMessage(
                userId,
                user.getNickname(),
                user.getProfileImageUrl(),
                null, null, null,
                "STOPPED",
                LocalDateTime.now().toString()
        );

        redisPublisher.publishLocation(crewId, message);

        log.info("위치 공유 종료: userId={}, crewId={}", userId, crewId);
    }

    // ---- 공통 메서드 ----

    // WebSocket 세션에서 userId 추출
    private UUID extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        return UUID.fromString(headerAccessor.getUser().getName());
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
