package com.main.heatrun.domain.auth.service;

import com.main.heatrun.domain.auth.dto.LoginRequest;
import com.main.heatrun.domain.auth.dto.RefreshRequest;
import com.main.heatrun.domain.auth.dto.RegisterRequest;
import com.main.heatrun.domain.auth.dto.TokenResponse;
import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.entity.UserLevel;
import com.main.heatrun.domain.repository.UserLevelRepository;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.exception.BusinessException;
import com.main.heatrun.global.security.jwt.JwtProvider;
import com.main.heatrun.global.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserLevelRepository userLevelRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final NicknameGenerator nicknameGenerator;

    // 회원가입
    @Transactional
    public TokenResponse register(RegisterRequest request) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
        }

        // 닉네임 유니크 생성
        String uniqueNickname = nicknameGenerator.generate(request.nickname());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 유저 생성 + 저장
        User user = User.createLocalUser(
                request.email(),
                uniqueNickname,
                encodedPassword
        );
        userRepository.save(user);

        // 유저 레벨 초기화 (회원가입 시 자동 생성)
        UserLevel userLevel = UserLevel.create(user);
        userLevelRepository.save(userLevel);

        // JWT 발급
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        log.info("회원가입 완료: {} (닉네임: {})", user.getEmail(), uniqueNickname);

        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getNickname(),
                user.getEmail()
        );
    }

    // 로그인
    @Transactional
    public TokenResponse login(LoginRequest request) {

        // 이메일로 유저 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        "이메일 또는 비밀번호가 올바르지 않습니다.",
                        HttpStatus.UNAUTHORIZED));

        // 계정 활성 상태 체크
        if (!user.isActive()) {
            throw new BusinessException(
                    "비활성화된 계정입니다.", HttpStatus.UNAUTHORIZED);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(
                    "이메일 또는 비밀번호가 올바르지 않습니다.",
                    HttpStatus.UNAUTHORIZED);
        }
        // JWT 발급 (Sliding - 로그인마다 리프레시 토큰 갱신)
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        log.info("로그인 완료: {}", user.getEmail());

        return new TokenResponse(
                accessToken,
                refreshToken,
                user.getNickname(),
                user.getEmail()
        );
    }

    //토큰 재발급
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {

        // 리프레시 토큰 유효성 검사
        if (!jwtProvider.validateToken(request.refreshToken())) {
            throw new BusinessException(
                    "유효하지 않은 리프레시 토큰입니다.",
                    HttpStatus.UNAUTHORIZED);
        }

        // 리프레시 토큰에서 userId 추출
        UUID userId = jwtProvider.getUserId(request.refreshToken());

        // Redis에 저장된 리프레시 토큰과 비교 (RTR 검증)
        String storedToken = jwtProvider.getRefreshToken(userId);
        if (storedToken == null || !storedToken.equals(request.refreshToken())) {
            // 탈취된 토큰으로 요청 -> 블랙리스트 등록
            jwtProvider.addBlacklist(userId);
            throw new BusinessException("이미 사용한 리프레시 토큰입니다. 다시 로그인해주세요.",
                    HttpStatus.UNAUTHORIZED);
        }

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("유저를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        // 계정 활성 상태 체크
        if (!user.isActive()) {
            throw new BusinessException("비활성화된 계정입니다.", HttpStatus.UNAUTHORIZED);
        }

        // 새 토큰 발급 (Sliding - 리프레시 토큰도 갱신)
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        log.info("토큰 재발급 완료: {}", user.getEmail());

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                user.getNickname(),
                user.getEmail()
        );
    }

    // 로그아웃
    @Transactional
    public void logout(UUID userId) {

        // Redis에서 리프레시 토큰 삭제
        jwtProvider.deleteRefreshToken(userId);
        log.info("로그아웃 완료: {}", userId);
    }
}
