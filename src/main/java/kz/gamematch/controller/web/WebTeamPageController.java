package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.dto.chat.SendChatMessageDto;
import kz.gamematch.service.chat.ChatService;
import kz.gamematch.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebTeamPageController extends WebSessionSupport {

    private final TeamService teamService;
    private final ChatService chatService;

    @GetMapping("/teams")
    public String teams(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        model.addAttribute("teams", teamService.getTeamsByUserId(currentUserId(session)));
        return "teams";
    }

    @GetMapping("/teams/{teamId}")
    public String teamDetails(@PathVariable Long teamId, Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        Long userId = currentUserId(session);
        addSessionAttributes(model, session);
        model.addAttribute("team", teamService.getTeamById(teamId));
        model.addAttribute("messages", chatService.getMessages(teamId, userId));
        return "team-detail";
    }

    @PostMapping("/teams/{teamId}/messages")
    public String sendMessage(
            @PathVariable Long teamId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            SendChatMessageDto dto = new SendChatMessageDto();
            dto.setSenderId(currentUserId(session));
            dto.setContent(content);
            chatService.sendMessage(teamId, dto);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/teams/" + teamId;
    }
}
