package kz.gamematch.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class SuggestedPlayerResponseDto {
    private Long profileId;
    private Long userId;
    private String nickname;
    private BigDecimal karma;
    private long requestCount;
}
