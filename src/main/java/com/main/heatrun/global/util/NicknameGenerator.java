package com.main.heatrun.global.util;

import com.main.heatrun.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NicknameGenerator {

    private final UserRepository userRepository;

    // 최대 닉네임 길이 - suffix "#1234" 5자 포함 고려
    private static final int MAX_BASE_LENGTH = 15;
    // suffix 범위 1000~9999
    private static final int SUFFIX_MIN = 1000;
    private static final int SUFFIX_RANGE = 9000;

    // 유니크 닉네임 생성
    public String generate(String baseNickname) {

        // 기본 닉네임 길이 제한
        String trimmed = trim(baseNickname);

        // 중복일때만 suffix
//        if (!userRepository.existsByNickname(trimmed)) {
//            return trimmed;
//        }

        // suffix 붙여서 재시도
        return generateWithSuffix(trimmed);
    }

    private String generateWithSuffix(String baseNickname) {
        String nickname;
        int attempts = 0;

        do {
            int suffix = SUFFIX_MIN + (int) (Math.random() * SUFFIX_RANGE);
            nickname = baseNickname + "#" + suffix;
            attempts++;

            // 무한루프 방지 - 100번 시도 후 타임스탬프 사용
            if (attempts >= 100) {
                nickname = baseNickname + "#" + System.currentTimeMillis() % 10000;
                log.warn("닉네임 suffix 100회 충돌 -> 타임스탬프 사용: {}", nickname);
                break;
            }

            log.debug("닉네임 생성 시도 {}: {}", attempts, nickname);
        } while (userRepository.existsByNickname(nickname));
        return nickname;
    }

    // 길이 제한 (15자)
    private String trim(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "유저" + (int) (Math.random() * 9000 + 1000);
        }
        return nickname.length() > MAX_BASE_LENGTH ? nickname.substring(0, MAX_BASE_LENGTH) : nickname;
    }
}
