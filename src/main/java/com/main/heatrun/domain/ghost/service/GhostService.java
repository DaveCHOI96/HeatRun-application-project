package com.main.heatrun.domain.ghost.service;

import com.main.heatrun.domain.entity.GhostRecord;
import com.main.heatrun.domain.ghost.dto.GhostRecordResponse;
import com.main.heatrun.domain.repository.GhostRecordRepository;
import com.main.heatrun.global.enums.RecordType;
import com.main.heatrun.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GhostService {

    private final GhostRecordRepository ghostRecordRepository;

    // 내 고스트 목록
    @Transactional(readOnly = true)
    public List<GhostRecordResponse> getMyGhosts(UUID userId) {
        return ghostRecordRepository
                .findByOwnerUserId(userId)
                .stream()
                .map(GhostRecordResponse::from)
                .collect(Collectors.toList());
    }

    // 공개 고스트 목록
    @Transactional(readOnly = true)
    public List<GhostRecordResponse> getPublicGhosts() {
        return ghostRecordRepository
                .findByIsPublicTrueOrderByDurationSecondsAsc()
                .stream()
                .map(GhostRecordResponse::from)
                .collect(Collectors.toList());
    }

    // 공개 전환
    @Transactional
    public GhostRecordResponse makePublic(UUID userId, UUID ghostId) {
        GhostRecord record = findMyGhost(userId, ghostId);
        record.makePublic();
        return GhostRecordResponse.from(record);
    }

    // 비공개 전환
    @Transactional
    public GhostRecordResponse makePrivate(UUID userId, UUID ghostId) {
        GhostRecord record = findMyGhost(userId, ghostId);
        record.makePrivate();
        return GhostRecordResponse.from(record);
    }

    private GhostRecord findMyGhost(UUID userId, UUID ghostId) {
        GhostRecord record = ghostRecordRepository.findById(ghostId)
                .orElseThrow(() -> new BusinessException(
                        "고스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!record.getOwnerUser().getId().equals(userId)) {
            throw new BusinessException("접근 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        return record;
    }
}
