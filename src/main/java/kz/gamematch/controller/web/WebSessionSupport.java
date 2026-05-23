package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

abstract class WebSessionSupport {

    protected Long currentUserId(HttpSession session) {
        Object value = session.getAttribute("userId");
        return value instanceof Long userId ? userId : null;
    }

    protected boolean isLoggedIn(HttpSession session) {
        return currentUserId(session) != null;
    }

    protected String redirectToLoginIfNeeded(HttpSession session) {
        return isLoggedIn(session) ? null : "redirect:/login";
    }

    protected void addSessionAttributes(Model model, HttpSession session) {
        model.addAttribute("currentUserId", session.getAttribute("userId"));
        model.addAttribute("currentEmail", session.getAttribute("email"));
        model.addAttribute("currentRole", session.getAttribute("role"));
        model.addAttribute("currentNickname", session.getAttribute("nickname"));
    }
}
