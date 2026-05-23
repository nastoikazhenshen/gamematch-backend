package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebProfileController extends WebSessionSupport {

    private final ProfileService profileService;

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
        model.addAttribute("profileForm", form);
        return "profile";
    }

    @PostMapping("/profiles/me")
    public String updateMyProfile(
            @ModelAttribute UpdateProfileRequestDto form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            ProfileResponseDto profile = profileService.updateProfile(currentUserId(session), form);
            session.setAttribute("nickname", profile.getNickname());
            redirectAttributes.addFlashAttribute("success", "Профиль обновлен");
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
        if (nickname != null && !nickname.isBlank()) {
            try {
                model.addAttribute("profile", profileService.searchByNickname(nickname));
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
        model.addAttribute("profile", profileService.getProfileById(profileId));
        return "profile-view";
    }
}
