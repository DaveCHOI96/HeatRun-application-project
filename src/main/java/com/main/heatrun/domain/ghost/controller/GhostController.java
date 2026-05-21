package com.main.heatrun.domain.ghost.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.ghost.dto.GhostRecordResponse;
import com.main.heatrun.domain.ghost.service.GhostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
