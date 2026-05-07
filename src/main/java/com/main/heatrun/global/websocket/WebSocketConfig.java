package com.main.heatrun.global.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // /topic -> 1:N 브로드캐스트 (크루 전체에게 위치 전송)
        // /queue -> 1:1 메시지 (개인 응원 수신)
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트 -> 서버 메시지 prefix
        // /app/location -> @MessageMapping("/location") 매핑
        registry.setApplicationDestinationPrefixes("/app");

        // 개인 메시지 prefix
        // /user/{userId}/queue/cheer -> 특정 유저에게 전송
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        // ws://localhost:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 운영 시 앱 도메인으로 제한
                .withSockJS(); // SockJS 풀백 지원
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 모든 WebSocket 메시지에 JWT 인증 인터셉터 적용
        registration.interceptors(webSocketAuthInterceptor);
    }
}
