package com.main.heatrun.domain.ghost.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.ghost.dto.GhostRecordResponse;
import com.main.heatrun.domain.ghost.service.GhostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/ghosts")
@RequiredArgsConstructor
public class GhostController {

    private final GhostService ghostService;

    // 내 고스트 목록 조회 (PB + 공개 등록)
    @GetMapping("/me")
    public ResponseEntity<List<GhostRecordResponse>> getMyGhosts(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ghostService.getMyGhosts(user.getId()));
    }

    // 공개 고스트 목록 조회 (다른 유저 고스트 선택용)
    @GetMapping("/public")
    public ResponseEntity<List<GhostRecordResponse>> getPublicGhosts(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ghostService.getPublicGhosts());
    }

    // 고스트 공개 전환
    @PutMapping("/{ghostId}/public")
    public ResponseEntity<GhostRecordResponse> makePublic(
            @AuthenticationPrincipal User user, @PathVariable UUID ghostId) {
        return ResponseEntity.ok(ghostService.makePublic(user.getId(), ghostId));
    }

    // 고스트 비공개 전환
    @PutMapping("/{ghostId}/private")
    public ResponseEntity<GhostRecordResponse> makePrivate(
            @AuthenticationPrincipal User user, @PathVariable UUID ghostId) {
        return ResponseEntity.ok(ghostService.makePrivate(user.getId(), ghostId));
    }

}
