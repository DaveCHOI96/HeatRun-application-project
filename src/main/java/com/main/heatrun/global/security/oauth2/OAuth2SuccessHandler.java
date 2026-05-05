package com.main.heatrun.global.security.oauth2;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
//SimpleUrlAuthenticationSuccessHandler
//→ 리다이렉트 기능을 제공하는 Spring Security 기본 클래스
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 / 구글 구분해서 이메일 추출
        String email = extractEmail(attributes);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        // 앱으로 토근 전달 (딥링크 또는 쿼리 파라미터)
        String redirectUrl = "heatrun://auth/callback"
                + "?accessToken=" + accessToken
                + "&refreshToken=" + refreshToken;
        //왜 딥링크(heatrun://auth/callback)로 리다이렉트?
        //→ 소셜 로그인은 브라우저에서 진행됨
        //→ 완료 후 앱으로 돌아와야 함
        //→ heatrun:// → 앱이 등록한 커스텀 URL 스킴
        //→ 앱이 이 URL을 받아서 토큰 저장 후 메인 화면으로 이동

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private String extractEmail(Map<String, Object> attributes) {
        // 카카오 응답 구조
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount =
                    (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }
        // 구글 응답 구조
        return (String) attributes.get("email");
    }
}
