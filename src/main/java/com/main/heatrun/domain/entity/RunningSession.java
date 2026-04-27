package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.RunningStatus;
import com.main.heatrun.global.enums.WeatherCondition;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "running_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunningSession {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() {
        this.id = UuidV7Generator.generateIfAbsent(this.id);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Id", nullable = false)
    private User user;

    // 총 거리(Km)
    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    // 총 시간(초)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // 평균 페이스 (초/km)
    @Column(name = "avg_pace")
    private Double avgPace;

    // 소모 칼로리
    @Column(name = "calories_burned")
    private Double caloriesBurned;

    // 날씨 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition", length = 20)
    private WeatherCondition weatherCondition;

    // 러닝 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunningStatus status = RunningStatus.IN_PROGRESS;

    // 도전한 고스트 세션 - 고스트 러닝 시 참조 (없으면 null)
    // self-referencing FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ghost_session_id")
    private RunningSession ghostSession;

    // 러닝 시작 시간
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    // 러닝 종료 시간 - 진행중이면 null
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 일반 러닝 시작
    public static RunningSession start(User user, WeatherCondition weather) {
        RunningSession session = new RunningSession();
        session.user = user;
        session.weatherCondition = weather;
        session.startedAt = LocalDateTime.now();
        return session;
    }

    // 고스트 러닝 시작
    public static RunningSession startWithGhost(User user,
                                                WeatherCondition weather,
                                                RunningSession ghostSession) {
        RunningSession session = new RunningSession();
        session.user = user;
        session.weatherCondition = weather;
        session.ghostSession = ghostSession;
        session.startedAt = LocalDateTime.now();
        return session;
    }

    // 러닝 완료 처리
    public void complete(Double distanceKm, Integer durationSeconds,
                         Double avgPace, Double caloriesBurned) {
        this.totalDistanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.avgPace = avgPace;
        this.caloriesBurned = caloriesBurned;
        this.status = RunningStatus.COMPLETED;
        this.endedAt = LocalDateTime.now();
    }

    // 일시정지
    public void pause() {
        this.status = RunningStatus.PAUSED;
    }

    // 재개
    public void resume() {
        this.status = RunningStatus.IN_PROGRESS;
    }

    // 완료 여부 확인
    public boolean isCompleted() {
        return this.status == RunningStatus.COMPLETED;
    }

    // 고스트 러닝 여부 확인
    public boolean isGhostRun() {
        return this.ghostSession != null;
    }
}
