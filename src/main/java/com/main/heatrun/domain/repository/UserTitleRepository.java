package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.UserTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserTitleRepository extends JpaRepository<UserTitle, Long> {
    
    // 유저의 보유 칭호 전체
    List<UserTitle> findByUserId(UUID userId);
    
    // 유저의 장착 중인 칭호 - 최대 2개
    List<UserTitle> findByUserIdAndIsEquippedTrue(UUID userId);
    
    // 특정 칭호 보유 여부
    boolean existsByUserIdAndTitleId(UUID userId, UUID titleId);

    // 특정 칭호 조회
    Optional<UserTitle> findByUserIdAndTitleId(UUID userId, UUID titleId);

    // 장착 중인 칭호 수 - 2개 제한 체크
    @Query("""
           SELECT COUNT(ut) FROM UserTitle ut
           WHERE ut.user.id = :userId
           AND ut.isEquipped = true
           """)
    long countEquippedTitles(@Param("userId") UUID userId);
}
