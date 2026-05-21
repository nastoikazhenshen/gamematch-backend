package kz.gamematch.controller.request;

import jakarta.validation.Valid;
import kz.gamematch.dto.request.CreateTeammateRequestDto;
import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.service.request.TeammateRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class TeammateRequestController {

    private final TeammateRequestService teammateRequestService;

    @PostMapping
    public TeammateRequestResponseDto createRequest(
            @Valid @RequestBody CreateTeammateRequestDto requestDto
    ) {
        return teammateRequestService.createRequest(requestDto);
    }

    @GetMapping
    public List<TeammateRequestResponseDto> getAllActiveRequests(
            @RequestParam(required = false) Long gameId
    ) {
        if (gameId != null) {
            return teammateRequestService.getRequestsByGame(gameId);
        }

        return teammateRequestService.getAllActiveRequests();
    }

    @GetMapping("/{requestId}")
    public TeammateRequestResponseDto getRequestById(@PathVariable Long requestId) {
        return teammateRequestService.getRequestById(requestId);
    }

    @GetMapping("/my/{authorId}")
    public List<TeammateRequestResponseDto> getMyRequests(@PathVariable Long authorId) {
        return teammateRequestService.getMyRequests(authorId);
    }

    @DeleteMapping("/{requestId}")
    public void cancelRequest(
            @PathVariable Long requestId,
            @RequestParam Long authorId
    ) {
        teammateRequestService.cancelRequest(requestId, authorId);
    }
}
