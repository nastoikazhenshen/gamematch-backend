package kz.gamematch.controller.web;

import jakarta.servlet.http.HttpSession;
import kz.gamematch.entity.ComplaintStatus;
import kz.gamematch.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebAdminController extends WebSessionSupport {

    private final AdminService adminService;

    @GetMapping("/admin")
    public String adminPanel(
            @RequestParam(required = false) ComplaintStatus status,
            Model model,
            HttpSession session
    ) {
        String redirect = redirectToDashboardIfNotAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        String adminEmail = (String) session.getAttribute("email");
        addSessionAttributes(model, session);
        model.addAttribute("dashboard", adminService.getDashboard(adminEmail));
        model.addAttribute("users", adminService.getUsers(adminEmail));
        model.addAttribute("complaints", adminService.getComplaints(adminEmail, status));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ComplaintStatus.values());
        return "admin";
    }

    @PostMapping("/admin/users/{userId}/block")
    public String blockUser(
            @PathVariable Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToDashboardIfNotAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            adminService.blockUser((String) session.getAttribute("email"), userId);
            redirectAttributes.addFlashAttribute("success", "User blocked");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{userId}/unblock")
    public String unblockUser(
            @PathVariable Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToDashboardIfNotAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            adminService.unblockUser((String) session.getAttribute("email"), userId);
            redirectAttributes.addFlashAttribute("success", "User unblocked");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/complaints/{complaintId}/resolve")
    public String resolveComplaint(
            @PathVariable Long complaintId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return updateComplaint(complaintId, session, redirectAttributes, true);
    }

    @PostMapping("/admin/complaints/{complaintId}/dismiss")
    public String dismissComplaint(
            @PathVariable Long complaintId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return updateComplaint(complaintId, session, redirectAttributes, false);
    }

    @PostMapping("/admin/requests/inactive/delete")
    public String deleteInactiveRequests(
            @RequestParam(defaultValue = "7") int olderThanDays,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = redirectToDashboardIfNotAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            int deleted = adminService.deleteInactiveRequests((String) session.getAttribute("email"), olderThanDays);
            redirectAttributes.addFlashAttribute("success", "Deleted inactive requests: " + deleted);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }

    private String updateComplaint(
            Long complaintId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            boolean resolve
    ) {
        String redirect = redirectToDashboardIfNotAdmin(session);
        if (redirect != null) {
            return redirect;
        }

        try {
            if (resolve) {
                adminService.resolveComplaint((String) session.getAttribute("email"), complaintId);
                redirectAttributes.addFlashAttribute("success", "Complaint resolved");
            } else {
                adminService.dismissComplaint((String) session.getAttribute("email"), complaintId);
                redirectAttributes.addFlashAttribute("success", "Complaint dismissed");
            }
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin";
    }
}
