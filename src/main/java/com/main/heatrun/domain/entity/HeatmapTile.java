package com.main.heatrun.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "heatmap_tiles",
uniqueConstraints = {
        //유저 x 타일 x 룸레벨 복합 유니크
        @UniqueConstraint(name = "uk_heatmap_user_tile_zoom",
        columnNames = {"user_id", "tile_x", "tile_y", "zoom_level"})
},
indexes = {
        //히트맵 렌더링 쿼리 인덱스
        @Index(name = "idx_heatmap_user_zoom_tile",
        columnList = "user_id, zoom_level, tile_X, tile_y")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HeatmapTile {

    // 기본키 — IDENTITY (대용량, UUID v7과 성능 동일하나 단순성 선택)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 히트맵 소유 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 타일 X 좌표 - 지도 격자 X 인덱스
    @Column(name = "tile_x", nullable = false)
    private Integer tileX;

    // 타일 Y 좌표 - 지도 격자 Y 인덱스
    @Column(name = "tile_y", nullable = false)
    private Integer tileY;

    // 줌 레벨 - 지도 확대/축소 단계 (1~20)
    @Column(name = "zoom_level", nullable = false)
    private Integer zoomLevel;

    // 방문 횟수 - 중첩될수록 히트맵 진해짐
    // 그런데 어차피
    @Column(name = "visit_count", nullable = false)
    private Integer visitCount = 1;

    // 히트맵 강도 - 0.0 ~ 1.0, 방문 횟수 기반 계산
    @Column(nullable = false)
    private Double intensity = 1.0;

    // 러닝/보행 구분 - true: 러닝, false : 보행(만보기)
    @Column(name = "is_running", nullable = false)
    private Boolean isRunning = true;

    // 마지막 방문 시간
    @Column(name = "last_visited_at", nullable = false)
    private LocalDateTime lastVisitedAt;

    public static HeatmapTile create(User user, Integer tileX, Integer tileY,
                                     Integer zoomLevel, Boolean isRunning) {
        HeatmapTile tile = new HeatmapTile();
        tile.user = user;
        tile.tileX = tileX;
        tile.tileY = tileY;
        tile.zoomLevel = zoomLevel;
        tile.isRunning = isRunning;
        tile.lastVisitedAt = LocalDateTime.now();
        return tile;
    }

    // 방문 횟수 증가 및 강도 재계산
    public void increaseVisit() {
        this.visitCount++;
        this.intensity = Math.min(1.0, this.visitCount / 10.0);
        this.lastVisitedAt = LocalDateTime.now();
    }
}
