package kz.gamematch.controller.request;

import jakarta.validation.Valid;
import kz.gamematch.dto.response.CreateResponseDto;
import kz.gamematch.dto.response.RequestResponseDto;
import kz.gamematch.service.request.RequestResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestResponseController {

    private final RequestResponseService requestResponseService;

    @PostMapping("/requests/{requestId}/responses")
    public RequestResponseDto createResponse(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateResponseDto dto
    ) {
        return requestResponseService.createResponse(requestId, dto);
    }

    @GetMapping("/requests/{requestId}/responses")
    public List<RequestResponseDto> getResponsesByRequest(@PathVariable Long requestId) {
        return requestResponseService.getResponsesByRequest(requestId);
    }

    @PostMapping("/responses/{responseId}/accept")
    public RequestResponseDto acceptResponse(
            @PathVariable Long responseId,
            @RequestParam Long authorId
    ) {
        return requestResponseService.acceptResponse(responseId, authorId);
    }

    @PostMapping("/responses/{responseId}/reject")
    public RequestResponseDto rejectResponse(
            @PathVariable Long responseId,
            @RequestParam Long authorId
    ) {
        return requestResponseService.rejectResponse(responseId, authorId);
    }
}