package com.main.heatrun.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
//AuthenticationEntryPoint
//→ Spring Security가 401 상황에서 자동 호출
//→ 이게 없으면 Spring 기본 에러 페이지(HTML)가 반환됨
//→ 모바일 앱은 HTML 파싱 불가 → JSON으로 통일 필수
public class AuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    //ObjectMapper
    //→ Java 객체를 JSON 문자열로 변환
    //→ Map.of("status", 401, "message", "...") → {"status":401,"message":"..."}

    // 인증 실패 시 401 반환
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of(
                        "status", 401,
                        "message", "로그인이 필요합니다."
                ))
        );
    }
}
