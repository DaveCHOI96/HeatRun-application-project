package com.main.heatrun.domain.entity;

import com.main.heatrun.global.enums.ExpSourceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "exp_logs",
indexes = {
        @Index(name = "idx_exp_logs_user_created",
        columnList = "user_id, created_at"),
        @Index(name = "idx_exp_logs_source",
        columnList = "source_type, source_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExpLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 획득 경험치
    @Column(name = "exp_earned", nullable = false)
    private Integer expEarned;

    // 경험치 획득 출처
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private ExpSourceType sourceType;

    // 출처 ID - 러닝 세션 ID, 만보기 로그 ID 등 (없으면 null)
    @Column(name = "source_id", columnDefinition = "uuid")
    private UUID sourceId;

    public static ExpLog of(User user, Integer expEarned,
                            ExpSourceType sourceType, UUID sourceId) {
        ExpLog log = new ExpLog();
        log.user = user;
        log.expEarned = expEarned;
        log.sourceType = sourceType;
        log.sourceId = sourceId;
        return log;
    }
}
