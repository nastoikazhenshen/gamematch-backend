package kz.gamematch.controller.request;

import jakarta.validation.Valid;
import kz.gamematch.dto.response.CreateResponseDto;
import kz.gamematch.dto.response.RequestResponseDto;
import kz.gamematch.security.CurrentUserService;
import kz.gamematch.service.request.RequestResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestResponseController {

    private final RequestResponseService requestResponseService;
    private final CurrentUserService currentUserService;

    @PostMapping("/requests/{requestId}/responses")
    public RequestResponseDto createResponse(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody CreateResponseDto dto
    ) {
        dto.setResponderId(currentUserService.userId(currentUser));
        return requestResponseService.createResponse(requestId, dto);
    }

    @GetMapping("/requests/{requestId}/responses")
    public List<RequestResponseDto> getResponsesByRequest(@PathVariable Long requestId) {
        return requestResponseService.getResponsesByRequest(requestId);
    }

    @PostMapping("/responses/{responseId}/accept")
    public RequestResponseDto acceptResponse(
            @PathVariable Long responseId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return requestResponseService.acceptResponse(responseId, currentUserService.userId(currentUser));
    }

    @PostMapping("/responses/{responseId}/reject")
    public RequestResponseDto rejectResponse(
            @PathVariable Long responseId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return requestResponseService.rejectResponse(responseId, currentUserService.userId(currentUser));
    }
}
