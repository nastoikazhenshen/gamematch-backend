package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.dto.admin.CreateComplaintDto;
import kz.gamematch.dto.profile.PlayerGameResponseDto;
import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.dto.profile.UpsertPlayerGameRequestDto;
import kz.gamematch.repository.GameRankRepository;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.service.admin.ComplaintService;
import kz.gamematch.service.profile.ProfileService;
import kz.gamematch.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebProfileController extends WebSessionSupport {

    private static final List<String> TIMEZONES = List.of(
            "Asia/Qyzylorda",
            "Asia/Almaty",
            "Asia/Aqtobe",
            "Asia/Aqtau",
            "Asia/Atyrau",
            "Asia/Oral",
            "Europe/Moscow",
            "UTC"
    );

    private static final List<String> PLAY_TIME_OPTIONS = List.of(
            "00:00", "00:30", "01:00", "01:30", "02:00", "02:30",
            "03:00", "03:30", "04:00", "04:30", "05:00", "05:30",
            "06:00", "06:30", "07:00", "07:30", "08:00", "08:30",
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
            "15:00", "15:30", "16:00", "16:30", "17:00", "17:30",
            "18:00", "18:30", "19:00", "19:30", "20:00", "20:30",
            "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"
    );

    private final ProfileService profileService;
    private final GameRepository gameRepository;
    private final GameRankRepository gameRankRepository;
    private final TeamService teamService;
    private final ComplaintService complaintService;

    @GetMapping("/profiles/me")
    public String myProfile(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        ProfileResponseDto profile = profileService.getProfileByUserId(currentUserId(session));
        UpdateProfileRequestDto form = new UpdateProfileRequestDto();
        form.setNickname(profile.getNickname());
        form.setTimezone(profile.getTimezone());
        form.setAveragePlayTime(profile.getAveragePlayTime());
        form.setBio(profile.getBio());

        addSessionAttributes(model, session);
        model.addAttribute("profile", profile);
        model.addAttribute("stats", profileService.getStatsByUserId(profile.getUserId()));
        model.addAttribute("reviews", teamService.getReceivedReviews(profile.getUserId()));
        model.addAttribute("profileForm", form);
        model.addAttribute("playerGames", profileService.getProfileGamesByUserId(currentUserId(session)));
        model.addAttribute("games", gameRepository.findAll(Sort.by("name")));
        model.addAttribute("ranks", gameRankRepository.findAllWithGameOrder());
        model.addAttribute("timezones", TIMEZONES);
        model.addAttribute("playTimeOptions", PLAY_TIME_OPTIONS);
        model.addAttribute("playTimeFrom", playTimePart(profile.getAveragePlayTime(), 0));
        model.addAttribute("playTimeTo", playTimePart(profile.getAveragePlayTime(), 1));
        return "profile";
    }

    @PostMapping("/profiles/me")
    public String updateMyProfile(
            @ModelAttribute UpdateProfileRequestDto form,
            @RequestParam(required = false) String playTimeFrom,
            @RequestParam(required = false) String playTimeTo,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            form.setAveragePlayTime(buildAveragePlayTime(playTimeFrom, playTimeTo));
            ProfileResponseDto profile = profileService.updateProfile(currentUserId(session), form);
            session.setAttribute("nickname", profile.getNickname());
            redirectAttributes.addFlashAttribute("success", "Profile updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profiles/me";
    }

    @PostMapping("/profiles/me/games")
    public String addOrUpdateMyGame(
            @ModelAttribute UpsertPlayerGameRequestDto form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            profileService.addOrUpdatePlayerGame(currentUserId(session), form);
            redirectAttributes.addFlashAttribute("success", "Game added to profile");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profiles/me";
    }

    @PostMapping("/profiles/me/games/{playerGameId}")
    public String updateMyGame(
            @PathVariable Long playerGameId,
            @ModelAttribute UpsertPlayerGameRequestDto form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            profileService.updatePlayerGame(currentUserId(session), playerGameId, form);
            redirectAttributes.addFlashAttribute("success", "Profile game updated");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profiles/me";
    }

    @PostMapping("/profiles/me/games/{playerGameId}/delete")
    public String deleteMyGame(
            @PathVariable Long playerGameId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            profileService.deletePlayerGame(currentUserId(session), playerGameId);
            redirectAttributes.addFlashAttribute("success", "Game removed from profile");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profiles/me";
    }

    @GetMapping("/profiles/search")
    public String searchProfile(
            @RequestParam(required = false) String nickname,
            Model model,
            HttpSession session
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        model.addAttribute("query", nickname);
        model.addAttribute("suggestedPlayers", profileService.getSuggestedPlayers(currentUserId(session), 10));
        if (nickname != null && !nickname.isBlank()) {
            try {
                ProfileResponseDto profile = profileService.searchByNickname(nickname);
                model.addAttribute("profile", profile);
                model.addAttribute("stats", profileService.getStatsByUserId(profile.getUserId()));
                model.addAttribute("reviews", teamService.getReceivedReviews(profile.getUserId()));
                model.addAttribute("playerGames", profileService.getProfileGames(profile.getId()));
            } catch (RuntimeException ex) {
                model.addAttribute("error", ex.getMessage());
            }
        }
        return "profile-search";
    }

    @GetMapping("/profiles/{profileId}")
    public String viewProfile(@PathVariable Long profileId, Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        ProfileResponseDto profile = profileService.getProfileById(profileId);
        List<PlayerGameResponseDto> playerGames = profileService.getProfileGames(profileId);
        model.addAttribute("profile", profile);
        model.addAttribute("playerGames", playerGames);
        model.addAttribute("stats", profileService.getStatsByUserId(profile.getUserId()));
        model.addAttribute("reviews", teamService.getReceivedReviews(profile.getUserId()));
        model.addAttribute("canReport", !profile.getUserId().equals(currentUserId(session)));
        return "profile-view";
    }

    @PostMapping("/profiles/{profileId}/complaints")
    public String createComplaint(
            @PathVariable Long profileId,
            @RequestParam String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            ProfileResponseDto profile = profileService.getProfileById(profileId);
            CreateComplaintDto dto = new CreateComplaintDto();
            dto.setReporterId(currentUserId(session));
            dto.setReportedUserId(profile.getUserId());
            dto.setReason(reason);
            complaintService.createComplaint((String) session.getAttribute("email"), dto);
            redirectAttributes.addFlashAttribute("success", "Complaint sent to administrators");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profiles/" + profileId;
    }

    private String buildAveragePlayTime(String from, String to) {
        if ((from == null || from.isBlank()) && (to == null || to.isBlank())) {
            return null;
        }
        if (from == null || from.isBlank()) {
            return "until " + to;
        }
        if (to == null || to.isBlank()) {
            return "from " + from;
        }
        return from + "-" + to;
    }

    private String playTimePart(String averagePlayTime, int partIndex) {
        if (averagePlayTime == null || averagePlayTime.isBlank() || !averagePlayTime.contains("-")) {
            return "";
        }

        String[] parts = averagePlayTime.split("-", 2);
        if (parts.length <= partIndex) {
            return "";
        }

        String value = parts[partIndex].trim();
        return PLAY_TIME_OPTIONS.contains(value) ? value : "";
    }
}
