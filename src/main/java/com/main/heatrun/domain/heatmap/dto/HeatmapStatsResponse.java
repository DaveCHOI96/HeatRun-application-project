package com.main.heatrun.domain.heatmap.dto;

public record HeatmapStatsResponse(
        long totalTiles, // 총 방문 타일 수
        long runningTiles, // 러닝으로 방문한 타일 수
        long walkingTiles, // 보행으로 방문한 타일 수
        long totalVisitCount  // 총 방문 횟수
) {
}
