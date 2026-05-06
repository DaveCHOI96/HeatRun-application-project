package com.main.heatrun.domain.user.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.user.dto.UpdateLocationScopeRequest;
import com.main.heatrun.domain.user.dto.UpdateNicknameRequest;
import com.main.heatrun.domain.user.dto.UpdatePrivacyZoneRequest;
import com.main.heatrun.domain.user.dto.UserResponse;
import com.main.heatrun.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyInfo(user.getId()));
    }

    // 닉네임 변경
    @PutMapping("/me/nickname")
    public ResponseEntity<UserResponse> updateNickname(
            @AuthenticationPrincipal User user, @Validated @RequestBody UpdateNicknameRequest request) {
        return ResponseEntity.ok(userService.updateNickname(user.getId(), request));
    }

    // 프로필 이미지 변경 (S3 업로드 후 URL 전달)
    // S3 구현 전까지 URL 직접 입력으로 임시 처리
    @PutMapping("/me/profile-image")
    public ResponseEntity<UserResponse> updateProfileImage(
            @AuthenticationPrincipal User user, @RequestParam String imageUrl) {
        return ResponseEntity.ok(userService.updateProfileImage(user.getId(), imageUrl));
    }

    // 프라이버시 존 설정
    @PutMapping("/me/privacy-zone")
    public ResponseEntity<UserResponse> updatePrivacyZone(
            @AuthenticationPrincipal User user, @Validated @RequestBody UpdatePrivacyZoneRequest request) {
        return ResponseEntity.ok(userService.updatePrivacyZone(user.getId(), request));
    }

    // 위치 공유 범위 변경
    @PutMapping("/me/location-scope")
    public ResponseEntity<UserResponse> updateLocationScope(
            @AuthenticationPrincipal User user, @Validated @RequestBody UpdateLocationScopeRequest request) {
        return ResponseEntity.ok(userService.updateLocationScope(user.getId(), request));
    }

    // 계정 비활성화 (탈퇴)
    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal User user) {
        userService.deactivate(user.getId());
        return ResponseEntity.ok().build();
    }
}
