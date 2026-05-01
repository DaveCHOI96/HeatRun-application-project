package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserLevelRepository extends JpaRepository<UserLevel, UUID> {

    // 유저 레벨 조회 - 1:1 관계
    Optional<UserLevel> findByUserId(UUID userId);
}
