package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.dto.auth.AuthResponseDto;
import kz.gamematch.dto.auth.LoginRequestDto;
import kz.gamematch.dto.auth.RegisterRequestDto;
import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.security.jwt.JwtService;
import kz.gamematch.service.auth.AuthService;
import kz.gamematch.service.profile.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAuthController extends WebSessionSupport {

    private final AuthService authService;
    private final JwtService jwtService;
    private final ProfileService profileService;

    @GetMapping("/")
    public String root(HttpSession session) {
        return isLoggedIn(session) ? "redirect:/dashboard" : "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        session.invalidate();
        model.addAttribute("loginRequest", new LoginRequestDto());
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @ModelAttribute LoginRequestDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AuthResponseDto response = authService.login(request);
            fillSession(session, response.getToken());
            return "redirect:/dashboard";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        session.invalidate();
        model.addAttribute("registerRequest", new RegisterRequestDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute RegisterRequestDto request,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AuthResponseDto response = authService.register(request);
            fillSession(session, response.getToken());
            return "redirect:/dashboard";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logoutByGet(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private void fillSession(HttpSession session, String token) {
        Long userId = jwtService.extractUserId(token);
        session.setAttribute("token", token);
        session.setAttribute("userId", userId);
        session.setAttribute("email", jwtService.extractUsername(token));
        session.setAttribute("role", jwtService.extractRole(token));

        ProfileResponseDto profile = profileService.getProfileByUserId(userId);
        session.setAttribute("nickname", profile.getNickname());
    }
}
