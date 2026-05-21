package kz.gamematch.service.profile;

import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PlayerProfileRepository playerProfileRepository;

    public ProfileResponseDto getProfileByUserId(Long userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDto(profile);
    }

    public ProfileResponseDto getProfileById(Long profileId) {
        PlayerProfile profile = playerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDto(profile);
    }

    public ProfileResponseDto searchByNickname(String nickname) {
        PlayerProfile profile = playerProfileRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDto(profile);
    }

    public ProfileResponseDto updateProfile(Long userId, UpdateProfileRequestDto request) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getNickname() != null && !request.getNickname().equals(profile.getNickname())) {
            if (playerProfileRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("Nickname already exists");
            }
            profile.setNickname(request.getNickname());
        }

        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }

        if (request.getAveragePlayTime() != null) {
            profile.setAveragePlayTime(request.getAveragePlayTime());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        PlayerProfile savedProfile = playerProfileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    private ProfileResponseDto mapToDto(PlayerProfile profile) {
        return new ProfileResponseDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getNickname(),
                profile.getTimezone(),
                profile.getAveragePlayTime(),
                profile.getBio(),
                profile.getKarma(),
                profile.getCompletedMatches()
        );
    }
}
