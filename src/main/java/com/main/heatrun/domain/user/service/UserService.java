package com.main.heatrun.domain.user.service;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.domain.user.dto.UpdateNicknameRequest;
import com.main.heatrun.domain.user.dto.UserResponse;
import com.main.heatrun.global.exception.BusinessException;
import com.main.heatrun.global.security.jwt.JwtProvider;
import com.main.heatrun.global.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final NicknameGenerator nicknameGenerator;

    // SRID 4326 = WGS84 좌표계 (GPS 표준)
    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    //내 정보 조회
    @Transactional(readOnly = true)
    public UserResponse getMyInfo(UUID userId) {
        User user = findActiveUser(userId);
        return UserResponse.from(user);
    }

    // 닉네임 변경
    @Transactional
    public UserResponse updateNickname(UUID userId,
                                       UpdateNicknameRequest request) {
        User user = findActiveUser(userId);

        // 본인 현재 닉네임과 같으면 통과
        if (user.getNickname().equals(request.nickname())) {
            return UserResponse.from(user);
        }

        // 유니크 닉네임 생성
        String uniqueNickname = nicknameGenerator.generate(request.nickname());

        user.updateNickname(uniqueNickname);

        log.info("닉네임 변경: {} -> {}", user.getNickname(), uniqueNickname);
        return UserResponse.from(user);
    }

    // -- 공통 메서드 --

    // 활성 유저 조회 (없거나 비활성이면 예외 처리)
    private User findActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!user.isActive()) {
            throw new BusinessException("비활성화된 계정입니다.", HttpStatus.UNAUTHORIZED);
        }

        return user;
    }
}
