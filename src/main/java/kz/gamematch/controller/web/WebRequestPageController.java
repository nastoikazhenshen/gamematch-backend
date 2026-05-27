package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.dto.request.CreateTeammateRequestDto;
import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.dto.response.CreateResponseDto;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.GameRankRepository;
import kz.gamematch.service.request.RequestResponseService;
import kz.gamematch.service.request.TeammateRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class WebRequestPageController extends WebSessionSupport {

    private final TeammateRequestService teammateRequestService;
    private final RequestResponseService requestResponseService;
    private final GameRepository gameRepository;
    private final GameRankRepository gameRankRepository;

    @GetMapping("/requests")
    public String requests(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String minRank,
            @RequestParam(required = false) String maxRank,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desiredFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desiredTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        Page<TeammateRequestResponseDto> requests = teammateRequestService.searchActiveRequests(
                gameId,
                role,
                minRank,
                maxRank,
                desiredFrom,
                desiredTo,
                PageRequest.of(Math.max(page, 0), Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        addSessionAttributes(model, session);
        model.addAttribute("requests", requests);
        model.addAttribute("games", gameRepository.findAll(Sort.by("name")));
        model.addAttribute("ranks", gameRankRepository.findAllWithGameOrder());
        model.addAttribute("gameId", gameId);
        model.addAttribute("role", role);
        model.addAttribute("minRank", minRank);
        model.addAttribute("maxRank", maxRank);
        model.addAttribute("desiredFrom", desiredFrom);
        model.addAttribute("desiredTo", desiredTo);
        model.addAttribute("size", size);
        return "requests";
    }

    @GetMapping("/requests/my")
    public String myRequests(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        model.addAttribute("requests", teammateRequestService.getMyRequests(currentUserId(session)));
        return "my-requests";
    }

    @GetMapping("/requests/new")
    public String newRequest(Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        addSessionAttributes(model, session);
        model.addAttribute("games", gameRepository.findAll(Sort.by("name")));
        model.addAttribute("ranks", gameRankRepository.findAllWithGameOrder());
        return "request-form";
    }

    @PostMapping("/requests")
    public String createRequest(
            @RequestParam Long gameId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String requiredRole,
            @RequestParam(required = false) String minRank,
            @RequestParam(required = false) String maxRank,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desiredPlayTime,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            CreateTeammateRequestDto dto = new CreateTeammateRequestDto();
            dto.setAuthorId(currentUserId(session));
            dto.setGameId(gameId);
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setRequiredRole(requiredRole);
            dto.setMinRank(minRank);
            dto.setMaxRank(maxRank);
            dto.setDesiredPlayTime(desiredPlayTime);
            TeammateRequestResponseDto created = teammateRequestService.createRequest(dto);
            return "redirect:/requests/" + created.getId();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/requests/new";
        }
    }

    @GetMapping("/requests/{requestId}")
    public String requestDetails(@PathVariable Long requestId, Model model, HttpSession session) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        TeammateRequestResponseDto request = teammateRequestService.getRequestById(requestId);
        addSessionAttributes(model, session);
        model.addAttribute("request", request);
        model.addAttribute("responses", requestResponseService.getResponsesByRequest(requestId));
        model.addAttribute("isAuthor", request.getAuthorId().equals(currentUserId(session)));
        return "request-detail";
    }

    @PostMapping("/requests/{requestId}/responses")
    public String createResponse(
            @PathVariable Long requestId,
            @RequestParam String message,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            CreateResponseDto dto = new CreateResponseDto();
            dto.setResponderId(currentUserId(session));
            dto.setMessage(message);
            requestResponseService.createResponse(requestId, dto);
            redirectAttributes.addFlashAttribute("success", "Response sent");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/requests/" + requestId;
    }

    @PostMapping("/responses/{responseId}/accept")
    public String acceptResponse(
            @PathVariable Long responseId,
            @RequestParam Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            requestResponseService.acceptResponse(responseId, currentUserId(session));
            redirectAttributes.addFlashAttribute("success", "Response accepted, team and chat created");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/requests/" + requestId;
    }

    @PostMapping("/responses/{responseId}/reject")
    public String rejectResponse(
            @PathVariable Long responseId,
            @RequestParam Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            requestResponseService.rejectResponse(responseId, currentUserId(session));
            redirectAttributes.addFlashAttribute("success", "Response rejected");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/requests/" + requestId;
    }

    @PostMapping("/requests/{requestId}/cancel")
    public String cancelRequest(
            @PathVariable Long requestId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToLoginIfNeeded(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            teammateRequestService.cancelRequest(requestId, currentUserId(session));
            redirectAttributes.addFlashAttribute("success", "Request cancelled");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/requests/" + requestId;
    }
}
