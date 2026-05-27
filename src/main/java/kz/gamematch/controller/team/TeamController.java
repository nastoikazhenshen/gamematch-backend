package kz.gamematch.controller.team;

import jakarta.validation.Valid;
import kz.gamematch.dto.team.CompleteMatchRequestDto;
import kz.gamematch.dto.team.CreatePlayerReviewRequestDto;
import kz.gamematch.dto.team.PlayerReviewResponseDto;
import kz.gamematch.dto.team.TeamResponseDto;
import kz.gamematch.security.CurrentUserService;
import kz.gamematch.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final CurrentUserService currentUserService;

    @GetMapping("/{teamId}")
    public TeamResponseDto getTeamById(@PathVariable Long teamId) {
        return teamService.getTeamById(teamId);
    }

    @GetMapping("/request/{requestId}")
    public TeamResponseDto getTeamByRequestId(@PathVariable Long requestId) {
        return teamService.getTeamByRequestId(requestId);
    }

    @GetMapping("/my/{userId}")
    public List<TeamResponseDto> getMyTeams(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long userId
    ) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return teamService.getTeamsByUserId(userId);
    }

    @PostMapping("/{teamId}/complete")
    public TeamResponseDto completeMatch(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody CompleteMatchRequestDto request
    ) {
        request.setUserId(currentUserService.userId(currentUser));
        return teamService.completeMatch(teamId, request.getUserId());
    }

    @PostMapping("/{teamId}/reviews")
    public PlayerReviewResponseDto reviewPlayer(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody CreatePlayerReviewRequestDto request
    ) {
        request.setReviewerId(currentUserService.userId(currentUser));
        return teamService.reviewPlayer(teamId, request);
    }

    @GetMapping("/{teamId}/reviews")
    public List<PlayerReviewResponseDto> getTeamReviews(@PathVariable Long teamId) {
        return teamService.getTeamReviews(teamId);
    }
}
