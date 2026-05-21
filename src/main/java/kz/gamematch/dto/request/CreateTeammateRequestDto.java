package kz.gamematch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTeammateRequestDto {

    @NotNull
    private Long authorId;

    @NotNull
    private Long gameId;

    @NotBlank
    @Size(max = 120)
    private String title;

    @Size(max = 2000)
    private String description;

    private String requiredRole;

    private String minRank;

    private String maxRank;

    private LocalDateTime desiredPlayTime;
}