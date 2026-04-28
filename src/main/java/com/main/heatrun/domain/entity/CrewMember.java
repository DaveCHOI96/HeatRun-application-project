package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.CrewRole;
import com.main.heatrun.global.enums.LocationShareScope;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crew_members",
uniqueConstraints = {
        // 크루 x 유저 복합 유니크 - 중복 가입 방지
        @UniqueConstraint(name = "uk_crew_member",
        columnNames = {"crew_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewMember {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() { this.id = UuidV7Generator.generateIfAbsent(this.id); }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 크루 내 역할 - 리더 / 일반 멤버
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrewRole role = CrewRole.MEMBER;

    // 크루 내 위치 공유 범위 - User 기본값 오버라이드 가능
    @Enumerated(EnumType.STRING)
    @Column(name = "location_share_scope", nullable = false, length = 20)
    private LocationShareScope locationShareScope = LocationShareScope.CREW_ALL;

    // 크루 가입 시간
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    // 멤버 조인
    public static  CrewMember join(Crew crew, User user) {
        CrewMember member = new CrewMember();
        member.crew = crew;
        member.user = user;
        member.role = CrewRole.MEMBER;
        member.joinedAt = LocalDateTime.now();
        return member;
    }

    // 리더 조인
    public static CrewMember joinAsLeader(Crew crew, User user) {
        CrewMember member = new CrewMember();
        member.crew = crew;
        member.user = user;
        member.role = CrewRole.LEADER;
        member.joinedAt = LocalDateTime.now();
        return member;
    }

    // 리더 위임
    public void promoteToLeader() {
        this.role = CrewRole.LEADER;
    }

    // 위치 공유 범위 변경 (크루별 오버라이드)
    public void updateLocationShareScope(LocationShareScope scope) {
        this.locationShareScope = scope;
    }

    // 리더 여부 확인
    public boolean isLeader() {
        return this.role == CrewRole.LEADER;
    }

}
