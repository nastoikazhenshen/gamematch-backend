package kz.gamematch.controller.request;

import jakarta.validation.Valid;
import kz.gamematch.dto.request.CreateTeammateRequestDto;
import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.service.request.TeammateRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public Page<TeammateRequestResponseDto> getAllActiveRequests(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String minRank,
            @RequestParam(required = false) String maxRank,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desiredFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime desiredTo,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return teammateRequestService.searchActiveRequests(
                gameId,
                role,
                minRank,
                maxRank,
                desiredFrom,
                desiredTo,
                pageable
        );
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
