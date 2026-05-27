package kz.gamematch.controller.admin;

import kz.gamematch.dto.admin.AdminComplaintDto;
import kz.gamematch.dto.admin.AdminDashboardDto;
import kz.gamematch.dto.admin.AdminUserDto;
import kz.gamematch.entity.ComplaintStatus;
import kz.gamematch.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public AdminDashboardDto dashboard(@AuthenticationPrincipal UserDetails admin) {
        return adminService.getDashboard(admin.getUsername());
    }

    @GetMapping("/users")
    public List<AdminUserDto> users(@AuthenticationPrincipal UserDetails admin) {
        return adminService.getUsers(admin.getUsername());
    }

    @PostMapping("/users/{userId}/block")
    public AdminUserDto blockUser(
            @AuthenticationPrincipal UserDetails admin,
            @PathVariable Long userId
    ) {
        return adminService.blockUser(admin.getUsername(), userId);
    }

    @PostMapping("/users/{userId}/unblock")
    public AdminUserDto unblockUser(
            @AuthenticationPrincipal UserDetails admin,
            @PathVariable Long userId
    ) {
        return adminService.unblockUser(admin.getUsername(), userId);
    }

    @GetMapping("/complaints")
    public List<AdminComplaintDto> complaints(
            @AuthenticationPrincipal UserDetails admin,
            @RequestParam(required = false) ComplaintStatus status
    ) {
        return adminService.getComplaints(admin.getUsername(), status);
    }

    @PostMapping("/complaints/{complaintId}/resolve")
    public AdminComplaintDto resolveComplaint(
            @AuthenticationPrincipal UserDetails admin,
            @PathVariable Long complaintId
    ) {
        return adminService.resolveComplaint(admin.getUsername(), complaintId);
    }

    @PostMapping("/complaints/{complaintId}/dismiss")
    public AdminComplaintDto dismissComplaint(
            @AuthenticationPrincipal UserDetails admin,
            @PathVariable Long complaintId
    ) {
        return adminService.dismissComplaint(admin.getUsername(), complaintId);
    }
}
