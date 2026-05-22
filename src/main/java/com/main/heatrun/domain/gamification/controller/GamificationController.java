package com.main.heatrun.domain.gamification.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.gamification.dto.ExpLogResponse;
import com.main.heatrun.domain.gamification.dto.TitleResponse;
import com.main.heatrun.domain.gamification.dto.UserLevelResponse;
import com.main.heatrun.domain.gamification.dto.UserTitleResponse;
import com.main.heatrun.domain.gamification.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    // 내 레벨 / 경험치 조회
    @GetMapping("/level")
    public ResponseEntity<UserLevelResponse> getMyLevel(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gamificationService.getMyLevel(user.getId()));
    }

    // 경험치 획득 이력
    @GetMapping("/exp-logs")
    public ResponseEntity<List<ExpLogResponse>> getExpLogs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gamificationService.getExpLogs(user.getId()));
    }

    // 전체 칭호 목록 (도감)
    @GetMapping("/titles")
    public ResponseEntity<List<TitleResponse>> getAllTitles(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gamificationService.getAllTitles(user.getId()));
    }

    // 내 보유 칭호 목록
    @GetMapping("/titles/me")
    public ResponseEntity<List<UserTitleResponse>> getMyTitles(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(gamificationService.getMyTitles(user.getId()));
    }

    // 칭호 장착
    @PutMapping("/titles/{titleId}/equip")
    public ResponseEntity<Void> equipTitle(@AuthenticationPrincipal User user, @PathVariable UUID titleId) {
        gamificationService.equipTitle(user.getId(), titleId);
        return ResponseEntity.ok().build();
    }

    // 칭호 해제
    @PutMapping("/titles/{titleId}/unequip")
    public ResponseEntity<Void> unequipTitle(@AuthenticationPrincipal User user, @PathVariable UUID titleId) {
        gamificationService.unequipTitle(user.getId(), titleId);
        return ResponseEntity.ok().build();
    }
}
