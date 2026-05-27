package kz.gamematch.controller.admin;

import jakarta.validation.Valid;
import kz.gamematch.dto.admin.AdminComplaintDto;
import kz.gamematch.dto.admin.CreateComplaintDto;
import kz.gamematch.service.admin.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    public AdminComplaintDto createComplaint(
            @AuthenticationPrincipal UserDetails reporter,
            @Valid @RequestBody CreateComplaintDto request
    ) {
        return complaintService.createComplaint(reporter.getUsername(), request);
    }
}
