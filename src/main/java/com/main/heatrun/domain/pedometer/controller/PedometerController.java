package com.main.heatrun.domain.pedometer.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.pedometer.dto.PedometerLogResponse;
import com.main.heatrun.domain.pedometer.dto.UpdateStepRequest;
import com.main.heatrun.domain.pedometer.service.PedometerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedometer")
@RequiredArgsConstructor
public class PedometerController {

    private final PedometerService pedometerService;

    // 오늘 만보기 조회
    @GetMapping("/today")
    public ResponseEntity<PedometerLogResponse> getToday(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pedometerService.getToday(user.getId()));
    }

    // 만보기 업데이트
    @PutMapping("/today")
    public ResponseEntity<PedometerLogResponse> updateToday(
            @AuthenticationPrincipal User user, @RequestBody UpdateStepRequest request) {
        return ResponseEntity.ok(pedometerService.updateToday(user.getId(), request));
    }

    // 최근 7일 기록
    @GetMapping("/weekly")
    public ResponseEntity<List<PedometerLogResponse>> getWeekly(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(pedometerService.getWeekly(user.getId()));
    }
}
