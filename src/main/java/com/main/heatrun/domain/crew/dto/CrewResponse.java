package com.main.heatrun.domain.crew.dto;

import com.main.heatrun.domain.entity.Crew;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrewResponse(
        UUID id,
        String name,
        String description,
        String visibility,
        Integer maxMembers,
        Integer memberCount,
        String leaderNickname
) {
    public static CrewResponse from(Crew crew) {
        return new CrewResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getVisibility().name(),
                crew.getMaxMembers(),
                crew.getMemberCount(),
                crew.getLeaderUser().getNickname()
        );
    }
}
