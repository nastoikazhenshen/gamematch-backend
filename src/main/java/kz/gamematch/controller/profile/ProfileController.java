package kz.gamematch.controller.profile;

import jakarta.validation.Valid;
import kz.gamematch.dto.profile.PlayerGameResponseDto;
import kz.gamematch.dto.profile.PlayerStatsResponseDto;
import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.SuggestedPlayerResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.dto.profile.UpsertPlayerGameRequestDto;
import kz.gamematch.security.CurrentUserService;
import kz.gamematch.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me/{userId}")
    public ProfileResponseDto getMyProfile(@AuthenticationPrincipal UserDetails currentUser, @PathVariable Long userId) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.getProfileByUserId(userId);
    }

    @PutMapping("/me/{userId}")
    public ProfileResponseDto updateMyProfile(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.updateProfile(userId, request);
    }

    @GetMapping("/{profileId}")
    public ProfileResponseDto getProfileById(@PathVariable Long profileId) {
        return profileService.getProfileById(profileId);
    }

    @GetMapping("/search")
    public ProfileResponseDto searchByNickname(@RequestParam String nickname) {
        return profileService.searchByNickname(nickname);
    }

    @GetMapping("/suggested")
    public List<SuggestedPlayerResponseDto> getSuggestedPlayers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Long excludedUserId
    ) {
        return profileService.getSuggestedPlayers(excludedUserId, limit);
    }

    @GetMapping("/me/{userId}/stats")
    public PlayerStatsResponseDto getMyStats(@AuthenticationPrincipal UserDetails currentUser, @PathVariable Long userId) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.getStatsByUserId(userId);
    }

    @GetMapping("/me/{userId}/games")
    public List<PlayerGameResponseDto> getMyGames(@AuthenticationPrincipal UserDetails currentUser, @PathVariable Long userId) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.getProfileGamesByUserId(userId);
    }

    @PostMapping("/me/{userId}/games")
    public PlayerGameResponseDto addOrUpdateMyGame(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody UpsertPlayerGameRequestDto request
    ) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.addOrUpdatePlayerGame(userId, request);
    }

    @PutMapping("/me/{userId}/games/{playerGameId}")
    public PlayerGameResponseDto updateMyGame(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long userId,
            @PathVariable Long playerGameId,
            @Valid @RequestBody UpsertPlayerGameRequestDto request
    ) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        return profileService.updatePlayerGame(userId, playerGameId, request);
    }

    @DeleteMapping("/me/{userId}/games/{playerGameId}")
    public void deleteMyGame(
            @AuthenticationPrincipal UserDetails currentUser,
            @PathVariable Long userId,
            @PathVariable Long playerGameId
    ) {
        currentUserService.requireSelfOrAdmin(currentUser, userId);
        profileService.deletePlayerGame(userId, playerGameId);
    }

    @GetMapping("/{profileId}/games")
    public List<PlayerGameResponseDto> getProfileGames(@PathVariable Long profileId) {
        return profileService.getProfileGames(profileId);
    }

    @GetMapping("/{profileId}/stats")
    public PlayerStatsResponseDto getProfileStats(@PathVariable Long profileId) {
        return profileService.getStatsByProfileId(profileId);
    }
}
