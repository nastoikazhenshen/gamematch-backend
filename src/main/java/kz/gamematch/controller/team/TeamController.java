package kz.gamematch.controller.team;

import kz.gamematch.dto.team.TeamResponseDto;
import kz.gamematch.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/{teamId}")
    public TeamResponseDto getTeamById(@PathVariable Long teamId) {
        return teamService.getTeamById(teamId);
    }

    @GetMapping("/request/{requestId}")
    public TeamResponseDto getTeamByRequestId(@PathVariable Long requestId) {
        return teamService.getTeamByRequestId(requestId);
    }

    @GetMapping("/my/{userId}")
    public List<TeamResponseDto> getMyTeams(@PathVariable Long userId) {
        return teamService.getTeamsByUserId(userId);
    }
}
