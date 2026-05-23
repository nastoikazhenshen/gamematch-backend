package kz.gamematch.dto.team;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamMemberDto {

    private Long userId;

    private String nickname;
}
