package kz.gamematch.controller.profile;

import kz.gamematch.dto.profile.KarmaLeaderboardEntryDto;
import kz.gamematch.service.profile.KarmaLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class KarmaLeaderboardController {

    private final KarmaLeaderboardService karmaLeaderboardService;

    @GetMapping("/karma")
    public List<KarmaLeaderboardEntryDto> karmaLeaderboard(
            @RequestParam(defaultValue = "20") int limit
    ) {
        return karmaLeaderboardService.getLeaders(limit);
    }
}
