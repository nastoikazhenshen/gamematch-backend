package kz.gamematch.controller.admin;

import kz.gamematch.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class AdminRequestController {

    private final AdminService adminService;

    @DeleteMapping("/inactive")
    public int deleteInactiveRequests(
            @AuthenticationPrincipal UserDetails admin,
            @RequestParam(defaultValue = "7") int olderThanDays
    ) {
        return adminService.deleteInactiveRequests(admin.getUsername(), olderThanDays);
    }
}
