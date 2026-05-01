package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TitleRepository extends JpaRepository<Title, UUID> {

    // 코드로 칭호 조회 - 조건 달성 체크 시 사용
    Optional<Title> findByCode(String code);

    // 시즌 한정 칭호 목록
    List<Title> findByIsLimitedTrue();

    // 일반 칭호 목록
    List<Title> findByIsLimitedFalse();
}
