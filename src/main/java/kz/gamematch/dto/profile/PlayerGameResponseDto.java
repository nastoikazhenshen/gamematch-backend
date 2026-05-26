package kz.gamematch.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerGameResponseDto {

    private Long id;

    private Long gameId;

    private String gameName;

    private String rank;

    private String rankImageUrl;

    private String mainRole;
}
