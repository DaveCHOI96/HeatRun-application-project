package com.main.heatrun.domain.entity;

import com.main.heatrun.global.base.BaseEntity;
import com.main.heatrun.global.base.UuidV7Generator;
import com.main.heatrun.global.enums.LocationShareScope;
import com.main.heatrun.global.enums.Provider;
import com.main.heatrun.global.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

//@AllArgsConstructor 불필요
// = 정적 팩토리 메서드 방식에서 @AllArgsConstructor는 불필요하고 오히려 위험합니다.
// 필드 순서가 바뀌면 컴파일 에러 없이 값이 잘못 들어갈 수 있습니다.

//@Builder가 있으면 외부에서 User.builder().email().build() 로 생성이 가능해져서
// 정적 팩토리 메서드로만 생성을 강제하는 의미가 없어집니다.
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
//    @UuidGenerator // v4 사용시
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @PrePersist
    protected void generateId() {
        this.id = UuidV7Generator.generateIfAbsent(this.id);
    }

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column
    private String password;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    // 프라이버시 존 중심 좌표 - PostGIS Point (WGS84 좌표계 SRID 4326)
    // 집/직장 위치, 이 좌표 반경 내 히트맵/위치 마스킹 처리
    @Column(name = "privacy_zone_point", columnDefinition = "geometry(Point, 4326)")
    private Point privacyZonePoint;

    // 프라이버시 존 반경 - 단위 m, 기본값 500m
    @Column(name = "privacy_zone_radius")
    private Integer privacyZoneRadius = 500;

    // 위치 공유 범위
    @Enumerated(EnumType.STRING)
    @Column(name = "location_share_scope", nullable = false, length = 20)
    private LocationShareScope locationShareScope = LocationShareScope.CREW_ALL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;


    // 정적 팩토리 메서드
    public static User createLocalUser(String email, String nickname, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.password = encodedPassword;
        user.provider = Provider.LOCAL;
        return user;
    }

    public static User createSocialUser(String email,
                                        String nickname,
                                        Provider provider,
                                        String providerId) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        user.provider = provider;
        user.providerId = providerId;
        return user;
    }

    // 비즈니스 메서드
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePrivacyZone(Point point, Integer radius) {
        this.privacyZonePoint = point;
        this.privacyZoneRadius = radius;
    }

    public void updateLocationShareScope(LocationShareScope scope) {
        this.locationShareScope = scope;
    }

    // 계정 비활성화 (본인 탈퇴)
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    // 계정 정지 (관리자)
    public void ban() {
        this.status = UserStatus.BANNED;
    }

    // 활성 상태 여부 확인
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

}
