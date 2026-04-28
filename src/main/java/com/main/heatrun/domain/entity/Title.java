package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.ConditionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "titles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Title {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    // 칭호 고유 코드 - 서버 로직에서 조건 체크 기준
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    // 칭호 이름 - 앱 표시용
    @Column(nullable = false, length = 50)
    private String name;

    // 칭호 설명
    @Column(length = 200)
    private String description;

    // 달성 조건 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    private ConditionType conditionType;

    // 달성 조건 값 - conditionType에 따라 의미 다름
    // ex) GHOST_OVERTAKE + 20 -> 고스트 20번 추월
    @Column(name = "condition_value", nullable = false)
    private Integer conditionValue;

    // 칭호 아이콘 URL - S3 저장
    @Column(name = "icon_url")
    private String iconUrl;

    // 시즌 한정 칭호 여부
    @Column(name = "is_limited", nullable = false)
    private Boolean isLimited = false;

    public static Title create(String code, String name, String description,
                               ConditionType conditionType, Integer conditionValue,
                               Boolean isLimited) {
        Title title = new Title();
        title.code = code;
        title.name = name;
        title.description = description;
        title.conditionType = conditionType;
        title.conditionValue = conditionValue;
        title.isLimited = isLimited;
        return title;
    }
}
