package com.main.heatrun.domain.heatmap.controller;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.heatmap.dto.HeatmapStatsResponse;
import com.main.heatrun.domain.heatmap.dto.HeatmapTileResponse;
import com.main.heatrun.domain.heatmap.dto.UpdateHeatmapRequest;
import com.main.heatrun.domain.heatmap.service.HeatmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/heatmap")
@RequiredArgsConstructor
public class HeatmapController {

    private final HeatmapService heatmapService;

    // 히트맵 타일 업데이트 (러닝 완료 후 호출)
    @PostMapping("/tiles")
    public ResponseEntity<Void> updateHeatmap(
            @AuthenticationPrincipal User user,
            @Validated @RequestBody UpdateHeatmapRequest request) {
        heatmapService.updateHeatmap(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    // 내 히트맵 조회 (화면 범위 기반)
    @GetMapping("/me")
    public ResponseEntity<List<HeatmapTileResponse>> getMyHeatmap(
            @AuthenticationPrincipal User user,
            @RequestParam Double minLat, @RequestParam Double maxLat,
            @RequestParam Double minLng, @RequestParam Double maxLng,
            @RequestParam(defaultValue = "15") Integer zoomLevel) {
        return ResponseEntity.ok(
                heatmapService.getMyHeatmap(
                        user.getId(), minLat, maxLat, minLng, maxLng, zoomLevel));
    }

    // 크루 통합 히트맵 조회
    @GetMapping("/crew/{crewId}")
    public ResponseEntity<List<HeatmapTileResponse>> getCrewHeatmap(
            @AuthenticationPrincipal User user, @PathVariable UUID crewId,
            @RequestParam Double minLat, @RequestParam Double maxLat,
            @RequestParam Double minLng, @RequestParam Double maxLng,
            @RequestParam(defaultValue = "15") Integer zoomLevel) {
        return ResponseEntity.ok(
                heatmapService.getCrewHeatmap(
                        user.getId(), crewId, minLat, maxLat, minLng, maxLng, zoomLevel));
    }

    // 내 히트맵 통계
    @GetMapping("/me/stats")
    public ResponseEntity<HeatmapStatsResponse> getMyHeatmapStats(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(heatmapService.getMyHeatmapStatus(user.getId()));
    }
}
