package kz.gamematch.dto.team;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TeamResponseDto {

    private Long id;

    private Long requestId;

    private Long acceptedResponseId;

    private Long gameId;

    private String gameName;

    private List<TeamMemberDto> members;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private Long completedByUserId;
}
