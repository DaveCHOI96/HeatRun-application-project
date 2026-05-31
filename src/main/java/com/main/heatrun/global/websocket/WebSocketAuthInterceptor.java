package com.main.heatrun.global.websocket;

import com.main.heatrun.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        // CONNECT 시에만 JWT 검증
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // WebSocket 연결 헤더에서 토큰 추출
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // null 체크 및 형식 검증을 substring 이전에 실행
            if (authHeader != null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket 인증 헤더 없음 또는 형식 오류");
                throw new IllegalStateException("인증 토큰이 필요합니다.");
            }

            String token = authHeader.substring(7);

            if (jwtProvider.validateToken(token)) {
                UUID userId = jwtProvider.getUserId(token);

                if (jwtProvider.isBlacklisted(token, userId)) {
                    log.warn("블랙리스트 유저 WebSocket 접근 차단: {}", userId);
                    throw new IllegalStateException("접근이 차단된 계정입니다.");
                }

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userId.toString(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                accessor.setUser(authenticationToken);
                log.info("WebSocket 연결 인증 성공: userId={}", userId);
            } else {
                log.warn("WebSocket 연결 JWT 검증 실패");
                throw new IllegalStateException("유효하지 않은 토큰입니다.");
            }
        }
        return message;
    }
}
