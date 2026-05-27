package kz.gamematch.service.profile;

import kz.gamematch.dto.profile.KarmaLeaderboardEntryDto;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.repository.PlayerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KarmaLeaderboardService {

    private final PlayerProfileRepository playerProfileRepository;

    @Transactional(readOnly = true)
    public List<KarmaLeaderboardEntryDto> getLeaders(int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        List<PlayerProfile> profiles = playerProfileRepository.findKarmaLeaders(PageRequest.of(0, size));
        List<KarmaLeaderboardEntryDto> leaders = new ArrayList<>();

        for (int i = 0; i < profiles.size(); i++) {
            PlayerProfile profile = profiles.get(i);
            leaders.add(new KarmaLeaderboardEntryDto(
                    i + 1,
                    profile.getId(),
                    profile.getUser().getId(),
                    profile.getNickname(),
                    profile.getKarma(),
                    profile.getCompletedMatches()
            ));
        }

        return leaders;
    }
}
