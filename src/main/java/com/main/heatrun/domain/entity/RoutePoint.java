package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_points",
     indexes = {
        // 세션별 순서 조회 인덱스 - 러닝 경로 재현 시 사용
        @Index(name = "idx_route_points_session_sequence",
              columnList = "session_id, sequence_number"),
             // 시간 기반 파티셔닝 인덱스
             @Index(name = "idx_route_points_recorded_at",
             columnList = "recorded_at")
     })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePoint extends BaseTimeEntity {

    // 기본키 - IDENTITY (대용량 로그성 데이터, UUID 불필요)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 러닝 세션
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private RunningSession session;

    // GPS 좌표 - PostGIS Point (WGS84 좌표계 SRID 4326)
    @Column(nullable = false, columnDefinition = "geometry(Point, 4326)")
    private Point location;

    // 고도(m) - GPS 정확도에 따라 null 가능
    @Column
    private Double altitude;

    // 속도(m/s) - GPS 정확도에 따라 null 가능
    @Column
    private Double speed;

    // 좌표 순서 번호 - 경로 재현 시 순서 보장
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    // 좌표 기록 시간 - 실제 GPS 수집 시간
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public static RoutePoint of(RunningSession session, Point location,
                                Double altitude, Double speed,
                                Integer sequenceNumber) {
        RoutePoint point = new RoutePoint();
        point.session = session;
        point.location = location;
        point.altitude = altitude;
        point.speed = speed;
        point.sequenceNumber = sequenceNumber;
        point.recordedAt = LocalDateTime.now();
        return point;
    }

}
