package kz.gamematch.dto.profile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpsertPlayerGameRequestDto {

    @NotNull
    private Long gameId;

    @Size(max = 80)
    private String rank;

    @Size(max = 80)
    private String mainRole;
}
