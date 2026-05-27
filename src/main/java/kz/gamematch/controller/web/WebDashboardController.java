package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.service.cabinet.PlayerCabinetService;
import kz.gamematch.service.profile.KarmaLeaderboardService;
import kz.gamematch.service.profile.ProfileService;
import kz.gamematch.service.request.TeammateRequestService;
import kz.gamematch.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebDashboardController extends WebSessionSupport {

    private final ProfileService profileService;
    private final TeammateRequestService teammateRequestService;
    private final TeamService teamService;
    private final PlayerCabinetService playerCabinetService;
    private final KarmaLeaderboardService karmaLeaderboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        Long userId = currentUserId(session);
        addSessionAttributes(model, session);
        model.addAttribute("profile", profileService.getProfileByUserId(userId));
        model.addAttribute("stats", profileService.getStatsByUserId(userId));
        model.addAttribute("myRequests", teammateRequestService.getMyRequests(userId));
        model.addAttribute("myTeams", teamService.getTeamsByUserId(userId));
        model.addAttribute("pendingResponses", playerCabinetService.getMyResponses((String) session.getAttribute("email"), ResponseStatus.PENDING));
        model.addAttribute("matchHistory", playerCabinetService.getMatchHistory((String) session.getAttribute("email")));
        model.addAttribute("karmaLeaders", karmaLeaderboardService.getLeaders(5));
        return "dashboard";
    }
}
