package com.main.heatrun.domain.gamification.service;

import com.main.heatrun.domain.entity.UserLevel;
import com.main.heatrun.domain.entity.UserTitle;
import com.main.heatrun.domain.gamification.dto.ExpLogResponse;
import com.main.heatrun.domain.gamification.dto.TitleResponse;
import com.main.heatrun.domain.gamification.dto.UserLevelResponse;
import com.main.heatrun.domain.gamification.dto.UserTitleResponse;
import com.main.heatrun.domain.repository.ExpLogRepository;
import com.main.heatrun.domain.repository.TitleRepository;
import com.main.heatrun.domain.repository.UserLevelRepository;
import com.main.heatrun.domain.repository.UserTitleRepository;
import com.main.heatrun.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserLevelRepository userLevelRepository;
    private final ExpLogRepository expLogRepository;
    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;

    // 내 레벨 조회
    @Transactional(readOnly = true)
    public UserLevelResponse getMyLevel(UUID userId) {
        UserLevel level = userLevelRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("레벨 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return UserLevelResponse.from(level);
    }

    // 경험치 이력 조회
    @Transactional(readOnly = true)
    public List<ExpLogResponse> getExpLogs(UUID userId) {
        return expLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ExpLogResponse::from)
                .collect(Collectors.toList());
    }

    // 전체 칭호 목록 (도감)
    @Transactional(readOnly = true)
    public List<TitleResponse> getAllTitles(UUID userId) {
        // 내가 보유한 칭호 ID 목록
        List<UserTitle> myTitles = userTitleRepository.findByUserId(userId);
        Set<UUID> earnedTitleIds = myTitles.stream()
                .map(ut -> ut.getTitle().getId())
                .collect(Collectors.toSet());
        Set<UUID> equippedTitleIds = myTitles.stream()
                .filter(UserTitle::getIsEquipped)
                .map(ut -> ut.getTitle().getId())
                .collect(Collectors.toSet());

        return titleRepository.findAll()
                .stream()
                .map(title -> new TitleResponse(
                        title.getId(),
                        title.getCode(),
                        title.getName(),
                        title.getDescription(),
                        title.getConditionType().name(),
                        title.getConditionValue(),
                        title.getIconUrl(),
                        title.getIsLimited(),
                        earnedTitleIds.contains(title.getId()),
                        equippedTitleIds.contains(title.getId())
                ))
                .collect(Collectors.toList());
    }

    // 내 보유 칭호
    @Transactional(readOnly = true)
    public List<UserTitleResponse> getMyTitles(UUID userId) {
        return userTitleRepository.findByUserId(userId)
                .stream()
                .map(UserTitleResponse::from)
                .collect(Collectors.toList());
    }

    // 칭호 장착
    @Transactional
    public void equipTitle(UUID userId, UUID titleId) {
        // 최대 2개 제한
        long equippedCount = userTitleRepository
                .countEquippedTitles(userId);
        if (equippedCount >= 2) {
            throw new BusinessException("칭호는 최대 2개까지 장착 가능합니다.", HttpStatus.BAD_REQUEST);
        }

        UserTitle userTitle = userTitleRepository
                .findByUserIdAndTitleId(userId, titleId)
                .orElseThrow(() -> new BusinessException("=보유하지 않은 칭호입니다.", HttpStatus.NOT_FOUND));

        userTitle.equip();
        log.info("칭호 장착: userId={}, titleId={}", userId, titleId);
    }

    // 칭호 해제
    @Transactional
    public void unequipTitle(UUID userId, UUID titleId) {
        UserTitle userTitle = userTitleRepository
                .findByUserIdAndTitleId(userId, titleId)
                .orElseThrow(() -> new BusinessException("보유하지 않은 칭호입니다.", HttpStatus.NOT_FOUND));

        userTitle.unequip();
        log.info("칭호 해제: userId={}, titleId={}", userId, titleId);
    }
}
