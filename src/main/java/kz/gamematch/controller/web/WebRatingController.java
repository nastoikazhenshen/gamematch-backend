package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.service.profile.KarmaLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebRatingController extends WebSessionSupport {

    private final KarmaLeaderboardService karmaLeaderboardService;

    @GetMapping("/ratings")
    public String ratings(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        model.addAttribute("leaders", karmaLeaderboardService.getLeaders(50));
        return "ratings";
    }
}
