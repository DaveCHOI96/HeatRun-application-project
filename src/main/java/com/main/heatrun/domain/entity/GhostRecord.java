package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.RecordType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ghost_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GhostRecord {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    // 원본 러닝 세션 - 좌표는 이 세션의 RoutePoint 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_session_id", nullable = false)
    private RunningSession originalSession;

    // 고스트 소유 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    // 고스트 유형 - PB 자동 등록 / 수동 공개 등록
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 20)
    private RecordType recordType;

    // 공개 여부 - true면 다른 유저가 고스트로 선택 가능
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    // 총 거리(km)
    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    // 총 시간(초)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // PB 자동 등록
    public static GhostRecord createPersonalBest(User user,
                                                 RunningSession session) {
        GhostRecord record = new GhostRecord();
        record.ownerUser = user;
        record.originalSession = session;
        record.recordType = RecordType.PERSONAL_BEST;
        record.totalDistanceKm = session.getTotalDistanceKm();
        record.durationSeconds = session.getDurationSeconds();
        return record;
    }

    // 수동 공개 등록
    public static GhostRecord createPublic(User user,
                                           RunningSession session) {
        GhostRecord record = new GhostRecord();
        record.ownerUser = user;
        record.originalSession = session;
        record.recordType = RecordType.MANUAL_PUBLIC;
        record.isPublic = true;
        record.totalDistanceKm = session.getTotalDistanceKm();
        record.durationSeconds = session.getDurationSeconds();
        return record;
    }

    // 공개 전환
    public void makePublic() {
        this.isPublic = true;
    }

    // 비공개 전환
    public void makePrivate() {
        this.isPublic = false;
    }
}
