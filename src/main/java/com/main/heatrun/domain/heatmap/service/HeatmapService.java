package com.main.heatrun.domain.heatmap.service;

import com.main.heatrun.domain.entity.HeatmapTile;
import com.main.heatrun.domain.entity.User;
import com.main.heatrun.domain.heatmap.dto.HeatmapStatsResponse;
import com.main.heatrun.domain.heatmap.dto.HeatmapTileResponse;
import com.main.heatrun.domain.heatmap.dto.UpdateHeatmapRequest;
import com.main.heatrun.domain.repository.CrewMemberRepository;
import com.main.heatrun.domain.repository.HeatmapTileRepository;
import com.main.heatrun.domain.repository.UserRepository;
import com.main.heatrun.global.exception.BusinessException;
import com.main.heatrun.global.util.TileConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeatmapService {

    private final HeatmapTileRepository heatmapTileRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final TileConverter tileConverter;

    // 기본 줌 레벨 - 히트맵 저장 기준
    // 줌 15 = 약 1km 범위의 격자 크기
    private static final int DEFAULT_ZOOM = 15;

    // 히트맵 타일 업데이트
    @Transactional
    public void updateHeatmap(UUID userId, UpdateHeatmapRequest request) {
        User user = findActiveUser(userId);
        
        // GPS 좌표 목록 -> 타일로 변환 후 저장
        request.coordinates().forEach(coord -> {
            int tileX = tileConverter.longitudeToTileX(
                    coord.longitude(), request.zoomLevel());
            int tileY = tileConverter.latitudeToTileY(
                    coord.latitude(), request.zoomLevel());

            // 기존 타일 있으면 방문 횟수 증가
            // 없으면 신규 생성 (upsert 방식)
            heatmapTileRepository
                    .findByUserIdAndTileXAndTileYAndZoomLevel(
                            userId, tileX, tileY, request.zoomLevel())
                    .ifPresentOrElse(
                            HeatmapTile::increaseVisit, // 기존 타일 업데이트
                            () -> heatmapTileRepository.save(
                                    HeatmapTile.create(
                                            user,
                                            tileX,
                                            tileY,
                                            request.zoomLevel(),
                                            request.isRunning()
                                    )
                            )
                    );
        });
        log.info("히트맵 업데이트: userId={}, tiles={}", userId, request.coordinates().size());
    }

    // 내 히트맵 조회
    @Transactional(readOnly = true)
    public List<HeatmapTileResponse> getMyHeatmap(UUID userId, Double minLat, Double maxLat,
                                                  Double minLng, Double maxLng, Integer zoomLevel) {
        // 화면 범위에 해당하는 타일 범위 계산
        int[] range = tileConverter.getTileRange(
                minLat, maxLat, minLng, maxLng, zoomLevel);

        return heatmapTileRepository
                .findByUserIdAndZoomLevelAndTileRange(
                        userId, zoomLevel,
                        range[0], range[1], // minX, maxX
                        range[2], range[3]  // minY, maxY
                )
                .stream()
                .map(HeatmapTileResponse::from)
                .collect(Collectors.toList());
    }

    // 크루 통합 히트맵 조회
    @Transactional(readOnly = true)
    public List<HeatmapTileResponse> getCrewHeatmap(UUID userId, UUID crewId, Double minLat, Double maxLat,
                                                    Double minLng, Double maxLng, Integer zoomLevel) {
        // 크루 멤버인지 확인
        if (!crewMemberRepository.existsByCrewIdAndUserId(crewId, userId)) {
            throw new BusinessException("크루 멤버가 아닙니다.", HttpStatus.FORBIDDEN);
        }

        // 크루 멤버 ID 목록 조회
        List<UUID> memberIds = crewMemberRepository.findUserIdsByCrewId(crewId);

        // 화면 범위 타일 계산
        int[] range = tileConverter.getTileRange(
                minLat, maxLat, minLng, maxLng, zoomLevel);

        // 크루원 전체 히트맵 합산
        return heatmapTileRepository
                .findCrewHeatmap(
                        memberIds, zoomLevel,
                        range[0], range[1],
                        range[2], range[3]
                )
                .stream()
                .map(HeatmapTileResponse::from)
                .collect(Collectors.toList());
    }

    // 히트맵 통계
    @Transactional
    public HeatmapStatsResponse getMyHeatmapStatus(UUID userId) {
        List<HeatmapTile> allTiles = heatmapTileRepository
                .findAll()
                .stream()
                .filter(tile -> tile.getUser().getId().equals(userId))
                .collect(Collectors.toList());

        long totalTiles = allTiles.size();
        long runningTiles = allTiles.stream()
                .filter(HeatmapTile::getIsRunning)
                .count();
        long walkingTiles = totalTiles - runningTiles;
        long totalVisitCount = allTiles.stream()
                .mapToLong(HeatmapTile::getVisitCount)
                .sum();

        return new HeatmapStatsResponse(
                totalTiles,
                runningTiles,
                walkingTiles,
                totalVisitCount
        );
    }

    private User findActiveUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new BusinessException(
                        "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
