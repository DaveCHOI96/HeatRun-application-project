package com.main.heatrun.global.websocket;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.CrewMemberRepository;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.exception.BusinessException;
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
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LiveCrewController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final CrewMemberRepository crewMemberRepository;

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
                LocalDateTime.now()
        );

        // 크루 전체에게 브로드캐스트
        messagingTemplate.convertAndSend("/topic/crew/" + crewId + "/location", message);
        log.debug("위치 전송: userId={}, crewId={}, lat={}, lng={}",
                userId, crewId, request.latitude(), request.longitude());
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
    }

    // ---- 공통 메서드 ----

    // WebSocket 세션에서 userId 추출
    private UUID extractUserId(SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getUser().getName();
        return UUID.fromString(userId);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(
                        "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
