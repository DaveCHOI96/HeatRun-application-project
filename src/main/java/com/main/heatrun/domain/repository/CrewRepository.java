package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.Crew;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrewRepository extends JpaRepository<Crew, UUID> {

    // 크루 이름으로 조회 - 중복 체크
    Optional<Crew> findByName(String name);

    // 크루 이름 존재 여부
    boolean existsByName(String name);

    // 공개 크루 검색 - 이름 검색(페이징)
    @Query("""
           SELECT c FROM Crew c
           WHERE c.visibility = 'PUBLIC'
           AND c.name LIKE %:keyword%
           ORDER BY c.memberCount DESC
           """)
    Page<Crew> searchPublicCrews(
            @Param("keyword") String keyword,
            Pageable pageable);
    
    // 유저가 리더인 크루 목록
    List<Crew> findByLeaderUserId(UUID userId);
}
