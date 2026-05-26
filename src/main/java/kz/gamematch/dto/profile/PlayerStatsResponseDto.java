package kz.gamematch.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerStatsResponseDto {
    private Long userId;
    private long playedMatches;
    private long sentResponses;
    private long acceptedResponses;
    private int acceptedResponseRate;
    private long authoredRequests;
}
