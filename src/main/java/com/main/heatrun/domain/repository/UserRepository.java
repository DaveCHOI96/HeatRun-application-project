package com.main.heatrun.domain.repository;

import com.main.heatrun.domain.entity.User;
import com.main.heatrun.global.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 이메일로 유저 조회 — 로그인 시 사용
    Optional<User> findByEmail(String email);

    // 닉네임으로 유저 조회 - 중복 체크 시 사용
    Optional<User> findByNickname(String nickname);

    // 소셜 로그인 유저 조회 - provider + providerId 조합
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    // 이메일 존재 여부 - 회원가입 중복 체크
    boolean existsByEmail(String email);

    // ADMIN LOCAL 유저만 찾기
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            WHERE u.email = :email AND u.provider = 'LOCAL'
            """)
    boolean existsLocalUserByEmail(@Param("email") String email);

    // 닉네임 존재 여부 - 닉네임 중복 체크
    boolean existsByNickname(String nickname);


}
