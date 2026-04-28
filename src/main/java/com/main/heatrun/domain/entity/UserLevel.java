package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "user_levels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLevel {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    // 유저 - 1:1 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 현재 레벨 - 1부터 시작
    @Column(name = "current_level", nullable = false)
    private Integer currentLevel = 1;

    // 누적 경험치
    @Column(name = "total_exp", nullable = false)
    private Integer totalExp = 0;

    // 다음 레벨까지 필요한 경험치
    @Column(name = "exp_to_next_level", nullable = false)
    private Integer expToNextLevel = 100;

    public static  UserLevel create(User user) {
        UserLevel level = new UserLevel();
        level.user = user;
        return level;
    }

    // 경험치 추가 및 레벨업 처리
    public void addExp(Integer exp) {
        this.totalExp += exp;
        while (this.totalExp >= this.expToNextLevel) {
            this.totalExp -= this.expToNextLevel;
            this.currentLevel++;
            //  레벨업마다 필요 경험치 증가 (현재 레벨 x 100)
            this.expToNextLevel = this.currentLevel * 100;
        }
    }
}
