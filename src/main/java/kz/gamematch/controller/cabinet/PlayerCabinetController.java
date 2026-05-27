package kz.gamematch.controller.cabinet;

import kz.gamematch.dto.cabinet.MatchHistoryDto;
import kz.gamematch.dto.cabinet.MyResponseDto;
import kz.gamematch.dto.team.PlayerReviewResponseDto;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.service.cabinet.PlayerCabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/player/cabinet")
@RequiredArgsConstructor
public class PlayerCabinetController {

    private final PlayerCabinetService playerCabinetService;

    @GetMapping("/responses")
    public List<MyResponseDto> myResponses(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) ResponseStatus status
    ) {
        return playerCabinetService.getMyResponses(user.getUsername(), status);
    }

    @GetMapping("/history")
    public List<MatchHistoryDto> matchHistory(@AuthenticationPrincipal UserDetails user) {
        return playerCabinetService.getMatchHistory(user.getUsername());
    }

    @GetMapping("/reviews/received")
    public List<PlayerReviewResponseDto> receivedReviews(@AuthenticationPrincipal UserDetails user) {
        return playerCabinetService.getReceivedReviews(user.getUsername());
    }

    @GetMapping("/reviews/given")
    public List<PlayerReviewResponseDto> givenReviews(@AuthenticationPrincipal UserDetails user) {
        return playerCabinetService.getGivenReviews(user.getUsername());
    }
}
