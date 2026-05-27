package kz.gamematch.mapper;

import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerNicknameResolver {

    private final PlayerProfileRepository playerProfileRepository;

    public String nickname(Long userId) {
        return playerProfileRepository.findByUserId(userId)
                .map(PlayerProfile::getNickname)
                .orElse("Player " + userId);
    }

    public String requiredNickname(Long userId, String errorMessage) {
        return playerProfileRepository.findByUserId(userId)
                .map(PlayerProfile::getNickname)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }
}
