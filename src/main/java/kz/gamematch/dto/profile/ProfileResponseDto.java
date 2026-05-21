package kz.gamematch.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProfileResponseDto {
    private Long id;
    private Long userId;
    private String email;
    private String nickname;
    private String timezone;
    private String averagePlayTime;
    private String bio;
    private BigDecimal karma;
    private Integer completedMatches;
}