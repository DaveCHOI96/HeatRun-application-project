package com.main.heatrun.global.security;

import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.security.handler.AuthAccessDeniedHandler;
import com.main.heatrun.global.security.handler.AuthEntryPoint;
import com.main.heatrun.global.security.jwt.JwtFilter;
import com.main.heatrun.global.security.jwt.JwtProvider;
import com.main.heatrun.global.security.oauth2.OAuth2SuccessHandler;
import com.main.heatrun.global.security.oauth2.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final AuthEntryPoint authEntryPoint;
    private final AuthAccessDeniedHandler authAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)
                //→ CSRF 공격은 브라우저 쿠키 기반
                //→ JWT는 헤더 기반 → CSRF 공격 불가
                //→ 비활성화해도 안전

                // 세션 미사용 (JWT Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //→ 서버가 세션을 유지하지 않음
                //→ JWT가 모든 요청에서 인증 담당
                //→ 서버 확장(scale-out) 시 세션 공유 문제 없음

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/oauth2/**",
                                "/oauth2/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                                //→ 개발/테스트 시 Swagger UI로 API 확인 필요
                                //→ 운영 배포 시에는 제거 권장
                        ).permitAll()
                        // 나머지 전부 인증 필요
                        .anyRequest().authenticated()
                        //permitAll() → 토큰 없이 접근 가능
                        //authenticated() → 반드시 유효한 토큰 필요
                )

                // OAuth2 소셜 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(authAccessDeniedHandler)
                )

                // JWT 필터 등록
                //addFilterBefore
                //→ 특정 필터 앞에 JwtFilter를 삽입
                .addFilterBefore(
                        new JwtFilter(jwtProvider, userRepository),
                        UsernamePasswordAuthenticationFilter.class
                );
                //UsernamePasswordAuthenticationFilter
        //→ Spring Security의 기본 로그인 처리 필터
        //→ 이 필터보다 먼저 JWT 검증이 실행돼야 함
        //→ JWT가 유효하면 이후 Security 필터들이 인증된 상태로 처리
        return http.build();
    }

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //BCrypt
    //→ 비밀번호 해싱 알고리즘
    //→ 같은 비밀번호도 매번 다른 해시값 생성 (salt 자동 포함)
    //→ 레인보우 테이블 공격 방어

    //왜 @Bean으로 등록?
    //→ Service에서 @Autowired로 주입받아 사용
    //→ 싱글톤으로 관리되어 매번 생성하는 비용 없음
}
