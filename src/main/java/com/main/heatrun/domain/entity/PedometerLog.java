package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pedometer_logs",
uniqueConstraints = {
        // 유저 x 날짜 복합 유니크 - 하루 1개 row
        @UniqueConstraint(name = "uk_pedometer_user_date",
        columnNames = {"user_id", "log_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedometerLog {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 기록 날짜 - 하루 1row, upsert 처리
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    // 걸음 수 - 실시간 업데이트
    @Column(name = "step_count", nullable = false)
    private Integer stepCount = 0;

    // 일일 목표 걸음 수 - 기본값 10,000보
    @Column(name = "daily_goal", nullable = false)
    private Integer dailyGoal = 10000;

    @Column(name = "goal_achieved", nullable = false)
    private Boolean goalAchieved = false;

    // 환산 거리(km) - 걸음 수 x 평균 보폭(0.75m) 계산
    @Column(name = "distance_km")
    private Double distanceKm;

    public static PedometerLog create(User user, LocalDate logDate,
                                      Integer dailyGoal) {
        PedometerLog log = new PedometerLog();
        log.user = user;
        log.logDate = logDate;
        log.dailyGoal = dailyGoal;
        return log;
    }

    // 걸음 수 업데이트 (실시간 동기화)
    public void updateStepCount(Integer stepCount) {
        this.stepCount = stepCount;
        this.distanceKm = stepCount * 0.00075; // 퍙균 보폭 0.75m
        this.goalAchieved = stepCount >= this.dailyGoal;
    }

    // 목표 걸음 수 변경
    public void updateDailyGoal(Integer dailyGoal) {
        this.dailyGoal = dailyGoal;
        this.goalAchieved = this.stepCount >= dailyGoal;
    }
}
