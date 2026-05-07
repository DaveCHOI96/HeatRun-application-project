package com.main.heatrun.domain.crew.dto;

import com.main.heatrun.domain.entity.CrewMember;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrewMemberResponse(
        UUID userId,
        String nickname,
        String profileImageUrl,
        String role,
        LocalDateTime joinedAt
) {
    public static CrewMemberResponse from(CrewMember member) {
        return new CrewMemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getRole().name(),
                member.getJoinedAt()
        );
    }
}
