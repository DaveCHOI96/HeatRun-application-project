package com.main.heatrun.domain.crew.controller;

import com.main.heatrun.domain.crew.dto.*;
import com.main.heatrun.domain.crew.service.CrewService;
import com.main.heatrun.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crews")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;

    // 크루 생성
    @PostMapping
    public ResponseEntity<CrewResponse> createCrew(
            @AuthenticationPrincipal User user,
            @Validated @RequestBody CreateCrewRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(crewService.createCrew(user.getId(), request));
    }

    // 크루 검색 (공개 크루)
    @GetMapping("/search")
    public ResponseEntity<Page<CrewResponse>> searchCrews(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(crewService.searchCrews(keyword, pageable));
    }

    // 크루 상세 조회
    @GetMapping("/{crewId}")
    public ResponseEntity<CrewResponse> getCrew(
            @PathVariable UUID crewId) {
        return ResponseEntity.ok(crewService.getCrew(crewId));
    }

    // 내 크루 목록
    @GetMapping("/me")
    public ResponseEntity<List<CrewResponse>> getMyCrews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(crewService.getMyCrews(user.getId()));
    }

    // 크루 멤버 목록
    @GetMapping("/{crewId}/members")
    public ResponseEntity<List<CrewMemberResponse>> getCrewMembers(
            @PathVariable UUID crewId) {
        return ResponseEntity.ok(crewService.getCrewMembers(crewId));
    }

    // 크루 가입
    @PostMapping("/{crewId}/join")
    public ResponseEntity<CrewResponse> joinCrew(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId) {
        return ResponseEntity.ok(crewService.joinCrew(user.getId(), crewId));
    }

    // 크루 탈퇴
    @DeleteMapping("/{crewId}/leave")
    public ResponseEntity<Void> leaveCrew(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId) {
        crewService.leaveCrew(user.getId(), crewId);
        return ResponseEntity.ok().build();
    }

    // 크루 정보 수정 (리더만)
    @PutMapping("/{crewId}")
    public ResponseEntity<CrewResponse> updateCrew(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId,
            @Validated @RequestBody UpdateCrewRequest request) {
        return ResponseEntity.ok(
                crewService.updateCrew(user.getId(), crewId, request));
    }

    // 리더 위임 (리더만)
    @PutMapping("/{crewId}/leader/{targetUserId}")
    public ResponseEntity<Void> delegateLeader(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId, @PathVariable UUID targetUserId) {
        crewService.delegateLeader(user.getId(), crewId, targetUserId);
        return ResponseEntity.ok().build();
    }

    // 응원 보내기
    @PostMapping("/{crewId}/cheer/{receiverId")
    public ResponseEntity<Void> sendCheer(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId,
            @PathVariable UUID receiverId, @Validated @RequestBody CheerRequest request) {
        crewService.sendCheer(user.getId(), crewId, receiverId, request);
        return ResponseEntity.ok().build();
    }

    // 크루 삭제 (리더만)
    @DeleteMapping("/{crewId}")
    public ResponseEntity<Void> deleteCrew(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId) {
        crewService.deleteCrew(user.getId(), crewId);
        return ResponseEntity.ok().build();
    }
}
