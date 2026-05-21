package kz.gamematch.controller.profile;

import jakarta.validation.Valid;
import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me/{userId}")
    public ProfileResponseDto getMyProfile(@PathVariable Long userId) {
        return profileService.getProfileByUserId(userId);
    }

    @PutMapping("/me/{userId}")
    public ProfileResponseDto updateMyProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequestDto request
    ) {
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
}