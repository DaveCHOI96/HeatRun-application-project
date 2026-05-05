package com.main.heatrun.global.security.oauth2;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.enums.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
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
        String nickname;

        if (provider == Provider.KAKAO) {
            providerId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount =
                    (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> kakaoProfile =
                    (Map<String, Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            nickname = (String) kakaoProfile.get("nickname");
        } else { // GOOGLE
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
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

        // 기존 유저 조회 or 신규 가입
        String finalEmail = email;
        String finalNickname = nickname;
        String finalProviderId = providerId;

        User user = userRepository
                //1단계: provider + providerId로 조회
                //→ 기존 소셜 로그인 유저 → 바로 반환
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 이메일로 기존 가입 여부 확인
                    //2단계: email로 조회
                    //→ 같은 이메일로 다른 소셜 / 이메일로 가입한 유저
                    //→ 기존 계정 반환 (중복 가입 방지)
                    return userRepository.findByEmail(finalEmail)
                            .orElseGet(() -> userRepository.save(
                                    //3단계: 없으면 신규 가입
                                    //→ User.createSocialUser()로 생성
                                    //→ DB 저장
                                    User.createSocialUser(
                                            finalEmail,
                                            finalNickname,
                                            provider,
                                            finalProviderId
                                    )
                            ));
                });

        return oAuth2User;
    }
}
