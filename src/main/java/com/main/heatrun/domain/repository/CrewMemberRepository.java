package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrewMemberRepository extends JpaRepository<CrewMember, UUID> {

    // 특정 크루의 특정 유저 조회 - 가입 여부 확인
    Optional<CrewMember> findByCrewIdAndUserId(UUID crewId, UUID userId);

    // 크루 전체 멤버 조회
    List<CrewMember> findByCrewId(UUID crewId);

    // 유저가 속한 크루 목록
    List<CrewMember> findByUserId(UUID userId);

    // 크루 멤버 ID 목록만 추출 - 히트맵 통합 조회용
    @Query("""
           SELECT cm.user.id FROM CrewMember cm
           WHERE cm.crew.id = :crewId
           """)
    List<UUID> findUserIdsByCrewId(@Param("crewId") UUID crewId);

    // 가입 여부 확인
    boolean existsByCrewIdAndUserId(UUID crewId, UUID userId);

    // 크루 멤버 수
    long countByCrewId(UUID crewId);
}
