package kz.gamematch.dto.profile;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {

    @Size(min = 3, max = 80)
    private String nickname;

    @Size(max = 50)
    private String timezone;

    @Size(max = 100)
    private String averagePlayTime;

    @Size(max = 1000)
    private String bio;
}