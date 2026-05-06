package com.main.heatrun.domain.running.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.running.dto.CompleteRunningRequest;
import com.main.heatrun.domain.running.dto.RoutePointRequest;
import com.main.heatrun.domain.running.dto.RunningSessionResponse;
import com.main.heatrun.domain.running.dto.StartRunningRequest;
import com.main.heatrun.domain.running.service.RunningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/running-sessions")
@RequiredArgsConstructor
public class RunningController {

    private final RunningService runningService;

    // 러닝 시작
    @PostMapping("/start")
    public ResponseEntity<RunningSessionResponse> startRunning(
            @AuthenticationPrincipal User user, @Validated @RequestBody StartRunningRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(runningService.startRunning(user.getId(), request));
    }

    // GPS 좌표 저장
    @PostMapping("/{sessionId}/route-points")
    public ResponseEntity<Void> saveRoutePoint(
            @AuthenticationPrincipal User user, @PathVariable UUID sessionId,
            @Validated @RequestBody RoutePointRequest request) {
        runningService.saveRoutePoint(user.getId(), sessionId, request);
        return ResponseEntity.ok().build();
    }

    // 러닝 일시정지
    @PostMapping("/{sessionId}/pause")
    public ResponseEntity<RunningSessionResponse> pauseRunning(
            @AuthenticationPrincipal User user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(runningService.pauseRunning(user.getId(), sessionId));
    }

    // 러닝 재개
    @PostMapping("/{sessionId}/resume")
    public ResponseEntity<RunningSessionResponse> resumeRunning(
            @AuthenticationPrincipal User user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(runningService.resumeRunning(user.getId(), sessionId));
    }

    // 러닝 완료
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<RunningSessionResponse> completeRunning(
            @AuthenticationPrincipal User user, @PathVariable UUID sessionId,
            @Validated @RequestBody CompleteRunningRequest request) {
        return ResponseEntity.ok(runningService.completeRunning(user.getId(), sessionId, request));
    }

    // 러닝 기록 목록 조회
    @GetMapping
    public ResponseEntity<List<RunningSessionResponse>> getMyRunningSessions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(runningService.getMyRunningSessions(user.getId()));
    }

    // 러닝 상세 조회
    @GetMapping("/{sessionId}")
    public ResponseEntity<RunningSessionResponse> getRunningSession(
            @AuthenticationPrincipal User user, @PathVariable UUID sessionId) {
        return ResponseEntity.ok(runningService.getRunningSession(user.getId(), sessionId));
    }

    // GPS 경로 조회 (고스트 재현용)
    @GetMapping("/{sessionId}/route-points")
    public ResponseEntity<List<RoutePointRequest>> getRoutePoints(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(runningService.getRoutePoints(sessionId));
    }

}
