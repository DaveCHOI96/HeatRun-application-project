package com.main.heatrun.global.security.oauth2;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.enums.Provider;
import com.main.heatrun.global.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
//DefaultOAuth2UserService
//→ Spring Security가 소셜 로그인 처리를 위해 제공하는 기본 클래스
//→ super.loadUser()가 소셜 서버에서 유저 정보를 가져옴
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 소셜 로그인 제공자 (kakao / google)
        // registrationId
        //→ application.yml에서 등록한 이름 (kakao, google)
        //→ 어떤 소셜 로그인인지 구분하는 데 사용
        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId()
                .toUpperCase();
        Provider provider = Provider.valueOf(registrationId);
        //왜 toUpperCase()를 하나?
        //→ yml은 소문자 "kakao"
        //→ Enum은 대문자 "KAKAO"
        //→ 변환 없이 Provider.valueOf("kakao") 하면 에러

        // 제공자별 유저 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId;
        String email;
        String baseNickname;

        if (provider == Provider.KAKAO) {
            Object idObj = attributes.get("id");
            providerId = idObj != null ? idObj.toString() : null;

            Map<String, Object> kakaoAccount =
                    (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> kakaoProfile =
                    (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            baseNickname = (String) kakaoProfile.get("nickname");

            // 이메일 동의 여부 확인
            Boolean emailVerified = (Boolean) kakaoAccount.get("is_email_verified");
            if (!Boolean.TRUE.equals(emailVerified)) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("email_not_provided"),
                        "카카오 이메일 동의가 필요합니다. 다시 로그인 후 이메일 제공에 동의해주세요."
                );
            }
            email = (String) kakaoAccount.get("email");
        } else { // GOOGLE / 구글은 이메일 동의 체크박스가 없음 무조건 제공됨
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            baseNickname = (String) attributes.get("name");
        }
        //카카오 응답 JSON 구조
        //{
        //  "id": 1234567890,
        //  "kakao_account": {
        //    "email": "user@kakao.com",
        //    "profile": {
        //      "nickname": "홍길동"
        //    }
        //  }
        //}
        //
        //구글 응답 JSON 구조 (훨씬 단순)
        //{
        //  "sub": "google_unique_id",
        //  "email": "user@gmail.com",
        //  "name": "홍길동"
        //}
        //
        //각 소셜마다 응답 구조가 달라서 분기 처리 필요

        // 유니크 닉네임 생성 NicknameGenerator로 위임
        String uniqueNickname = nicknameGenerator.generate(baseNickname);


        // 기존 유저 조회 or 신규 가입
        String finalEmail = email;
        String finalProviderId = providerId;
        String finalNickname = uniqueNickname;

        userRepository
                //1단계: provider + providerId로 조회
                //→ 기존 소셜 로그인 유저 → 바로 반환
                .findByProviderAndProviderId(provider, providerId)
                .map(existingUser -> {
                    // 이멜일 변경됐으면 업데이트
                    if (!existingUser.getEmail().equals(finalEmail)) {
                        existingUser.updateEmail(finalEmail);
                        log.info("소셜 계정 이메일 업데이트: {} -> {}", existingUser.getEmail(), finalEmail);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                            // 신규 가입 - 이메일 중복 상관없이 항상 새 계정 생성
                            User newUser = userRepository.save(
                                    User.createSocialUser(
                                            finalEmail,
                                            finalNickname,
                                            provider,
                                            finalProviderId
                                    )
                            );
                            log.info("소셜 회원가입 완료: provider={}, email={}", provider, finalEmail);
                            return newUser;
                        });
        return oAuth2User;
    }
}
