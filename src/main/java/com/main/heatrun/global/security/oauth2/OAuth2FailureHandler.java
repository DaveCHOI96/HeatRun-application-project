package com.main.heatrun.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = URLEncoder.encode(
                exception.getMessage(), StandardCharsets.UTF_8);

        // 앱으로 에러 전달
        String redirectUrl = "heatrun://auth/callback?error=" + errorMessage;
        log.warn("소셜 로그인 실패: {}", exception.getMessage());

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
