package kz.gamematch.dto.profile;

import java.math.BigDecimal;

public record KarmaLeaderboardEntryDto(
        int position,
        Long profileId,
        Long userId,
        String nickname,
        BigDecimal karma,
        Integer completedMatches
) {
}
