package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.service.cabinet.PlayerCabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebCabinetController extends WebSessionSupport {

    private final PlayerCabinetService playerCabinetService;

    @GetMapping("/cabinet")
    public String cabinet(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        String email = (String) session.getAttribute("email");
        addSessionAttributes(model, session);
        model.addAttribute("pendingResponses", playerCabinetService.getMyResponses(email, ResponseStatus.PENDING));
        model.addAttribute("acceptedResponses", playerCabinetService.getMyResponses(email, ResponseStatus.ACCEPTED));
        model.addAttribute("rejectedResponses", playerCabinetService.getMyResponses(email, ResponseStatus.REJECTED));
        model.addAttribute("matchHistory", playerCabinetService.getMatchHistory(email));
        model.addAttribute("receivedReviews", playerCabinetService.getReceivedReviews(email));
        model.addAttribute("givenReviews", playerCabinetService.getGivenReviews(email));
        return "cabinet";
    }
}
