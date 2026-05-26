package kz.gamematch.dto.team;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteMatchRequestDto {

    @NotNull
    private Long userId;
}
