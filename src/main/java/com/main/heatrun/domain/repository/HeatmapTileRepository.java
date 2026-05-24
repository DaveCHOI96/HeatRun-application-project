package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.HeatmapTile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HeatmapTileRepository extends JpaRepository<HeatmapTile, Long> {

    // 특정 타일 조회 - upsert 처리 시 사용
    Optional<HeatmapTile> findByUserIdAndTileXAndTileYAndZoomLevel(
            UUID userId, Integer tileX, Integer tileY, Integer zoomLevel);

    // 화면에 보이는 범위의 히트맵 타일 조회 - 지도 렌더링
    @Query("""
           SELECT ht FROM HeatmapTile ht
           WHERE ht.user.id = :userId
           AND ht.zoomLevel = :zoomLevel
           AND ht.tileX BETWEEN :minX AND :maxX
           AND ht.tileY BETWEEN :minY AND :maxY
           """)
    List<HeatmapTile> findByUserIdAndZoomLevelAndTileRange(
            @Param("userId") UUID userId,
            @Param("zoomLevel") Integer zoomLevel,
            @Param("minX") Integer minX,
            @Param("maxX") Integer maxX,
            @Param("minY") Integer minY,
            @Param("maxY") Integer maxY);

    // 크루 통합 히트맵 - 크루원 정체 타일 합산 (영역 점령)
    @Query("""
           SELECT ht FROM HeatmapTile ht
           WHERE ht.user.id IN :userIds
           AND ht.zoomLevel = :zoomLevel
           AND ht.tileX BETWEEN :minX AND :maxX
           AND ht.tileY BETWEEN :minY AND :maxY
           """)
    List<HeatmapTile> findCrewHeatmap(
            @Param("userIds") List<UUID> userIds,
            @Param("zoomLevel") Integer zoomLevel,
            @Param("minX") Integer minX,
            @Param("maxX") Integer maxX,
            @Param("minY") Integer minY,
            @Param("maxY") Integer maxY);

    // 히트맵 통계 DB집계
    @Query("""
           SELECT
                COUNT(ht),
                SUM(ht.visitCount),
                SUM(CASE WHEN ht.isRunning = true THEN 1 ELSE 0 END),
                SUM(CASE WHEN ht.isRunning = false THEN 1 ELSE 0 END)
           FROM HeatmapTile ht
           WHERE ht.user.id = :userId
           """)
    List<Object[]> getStatsByUserId(@Param("userId") UUID userId);

    // 유저의 총 타일 수 - 탐험 통계
    long countByUserId(UUID userId);
}
