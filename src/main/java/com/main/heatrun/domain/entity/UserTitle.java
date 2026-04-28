package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_titles",
uniqueConstraints = {
        // 유저 x 칭호 복합 유니크 - 중복 획득 방지
        @UniqueConstraint(name = "uk_user_title",
        columnNames = {"user_id", "title_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTitle extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 보유한 칭호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "title_id", nullable = false)
    private Title title;

    // 장착 여부 - 최대 2개까지 장착 가능 (Service 레이어에서 제한)
    @Column(name = "is_equipped", nullable = false)
    private Boolean isEquipped = false;

    // 칭호 획득 기간
    @Column(name = "earned_at", nullable = false)
    private LocalDateTime earnedAt;

    public static UserTitle earn(User user, Title title) {
        UserTitle userTitle = new UserTitle();
        userTitle.user = user;
        userTitle.title = title;
        userTitle.earnedAt = LocalDateTime.now();
        return userTitle;
    }

    // 칭호 장착
    public void equip() {
        this.isEquipped = true;
    }

    // 칭호 해제
    public void unequip() {
        this.isEquipped = false;
    }
}
