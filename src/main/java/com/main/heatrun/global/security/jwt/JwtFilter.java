package com.main.heatrun.global.security.jwt;

import com.main.heatrun.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
//OncePerRequestFilter
//→ Spring의 필터 기반 클래스
//→ 하나의 HTTP 요청에 딱 한 번만 실행 보장
//→ 왜 중요? 필터가 여러 번 실행되면 인증이 중복 처리될 수 있음
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
            // 2. userId 추출
            UUID userId = jwtProvider.getUserId(token);

            // 3. 블랙리스트 체크 (강제 로그아웃 / 계정 정지)
            if (jwtProvider.isBlacklisted(userId)) {
                log.warn("블랙리스트 유저 접근 차단: {}", userId);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "접근이 차단된 계정입니다.");
                return;
                //→ return으로 필터 체인 즉시 중단
                //→ 이후 코드 실행 안됨
            }

            // 4. 유저 조회
            userRepository.findById(userId).ifPresent(user -> {

                // 5. 활성 상태 확인
                if (user.isActive()) {
                    // 6. SecurityContext에 인증 정보 저장
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    // Spring Security의 인증 정보 저장소
                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }
            });
        }
        filterChain.doFilter(request, response);
        //→ 다음 필터 또는 실제 Controller로 요청 전달
        //→ 반드시 호출해야 함, 안하면 요청이 멈춤
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
            //substring(7)
            //→ "Bearer " (7글자) 제거
            //→ 순수 토큰 문자열만 추출
        }
        return null;
    }
}
