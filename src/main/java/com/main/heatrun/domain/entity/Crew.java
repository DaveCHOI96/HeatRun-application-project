package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.Visibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "crews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leaderUser;

    // 크루 이름 - 중복 불가
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 크루 소개
    @Column(length = 200)
    private String description;

    // 공개 여부 - PUBLIC(검색 가능) / PRIVATE(초대 전용)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.PUBLIC;

    // 최대 인원 - MVP 기본 50명
    @Column(name = "max_members", nullable = false)
    private Integer maxMembers = 50;

    // 현재 인원 - 가입/탈퇴 시 업데이트
    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 1;

    public static Crew create(User leader, String name,
                              String description, Visibility visibility) {
        Crew crew = new Crew();
        crew.leaderUser = leader;
        crew.name = name;
        crew.description = description;
        crew.visibility = visibility;
        return crew;
    }

    // 크루 정보 수정
    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // 멤버 수 증가(가입)
    public void increaseMemberCount() {
        this.memberCount++;
    }

    // 멤버 수 감소(탈퇴)
    public void decreaseMemberCount() {
        if (this.memberCount > 1) this.memberCount--;
    }

    // 정원 초과 여부
    public boolean isFull() {
        return this.memberCount >= this.maxMembers;
    }

    // 공개 크루 여부
    public boolean isPublic() {
        return this.visibility == Visibility.PUBLIC;
    }

}
