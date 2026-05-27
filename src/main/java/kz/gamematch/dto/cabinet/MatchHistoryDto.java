package kz.gamematch.dto.cabinet;

import kz.gamematch.dto.team.TeamMemberDto;

import java.time.LocalDateTime;
import java.util.List;

public record MatchHistoryDto(
        Long teamId,
        Long requestId,
        String requestTitle,
        String gameName,
        List<TeamMemberDto> teammates,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long completedByUserId
) {
}
