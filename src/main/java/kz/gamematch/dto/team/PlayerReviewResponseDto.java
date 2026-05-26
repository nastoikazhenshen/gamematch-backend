package kz.gamematch.dto.team;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PlayerReviewResponseDto {
    private Long id;
    private Long teamId;
    private Long reviewerId;
    private String reviewerNickname;
    private Long reviewedUserId;
    private String reviewedUserNickname;
    private Integer stars;
    private String comment;
    private LocalDateTime createdAt;
}
