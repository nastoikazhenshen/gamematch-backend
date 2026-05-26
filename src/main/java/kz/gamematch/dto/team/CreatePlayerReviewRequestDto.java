package kz.gamematch.dto.team;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlayerReviewRequestDto {

    @NotNull
    private Long reviewerId;

    @NotNull
    private Long reviewedUserId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer stars;

    @Size(max = 1000)
    private String comment;
}
