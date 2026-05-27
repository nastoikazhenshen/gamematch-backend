package kz.gamematch.service.admin;

import kz.gamematch.dto.admin.AdminComplaintDto;
import kz.gamematch.dto.admin.AdminDashboardDto;
import kz.gamematch.dto.admin.AdminUserDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.ComplaintRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int INACTIVE_REQUEST_RETENTION_DAYS = 7;

    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final ComplaintRepository complaintRepository;
    private final TeammateRequestRepository teammateRequestRepository;

    @Transactional(readOnly = true)
    public AdminDashboardDto getDashboard(String adminEmail) {
        requireAdmin(adminEmail);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(INACTIVE_REQUEST_RETENTION_DAYS);

        return new AdminDashboardDto(
                userRepository.count(),
                userRepository.findAll().stream().filter(user -> Boolean.TRUE.equals(user.getIsBlocked())).count(),
                complaintRepository.countByStatus(ComplaintStatus.OPEN),
                teammateRequestRepository.countByStatus(RequestStatus.ACTIVE),
                teammateRequestRepository.countByStatusNotAndCreatedAtBefore(RequestStatus.ACTIVE, cutoff)
        );
    }

    @Transactional(readOnly = true)
    public List<AdminUserDto> getUsers(String adminEmail) {
        requireAdmin(adminEmail);

        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapUser)
                .toList();
    }

    @Transactional
    public AdminUserDto blockUser(String adminEmail, Long userId) {
        User admin = requireAdmin(adminEmail);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateBlockTarget(admin, target);
        target.setIsBlocked(true);

        return mapUser(userRepository.save(target));
    }

    @Transactional
    public AdminUserDto unblockUser(String adminEmail, Long userId) {
        User admin = requireAdmin(adminEmail);
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateBlockTarget(admin, target);
        target.setIsBlocked(false);

        return mapUser(userRepository.save(target));
    }

    @Transactional(readOnly = true)
    public List<AdminComplaintDto> getComplaints(String adminEmail, ComplaintStatus status) {
        requireAdmin(adminEmail);

        List<Complaint> complaints = status == null
                ? complaintRepository.findAllByOrderByCreatedAtDesc()
                : complaintRepository.findByStatusOrderByCreatedAtDesc(status);

        return complaints.stream().map(this::mapComplaint).toList();
    }

    @Transactional
    public AdminComplaintDto resolveComplaint(String adminEmail, Long complaintId) {
        User admin = requireAdmin(adminEmail);
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setResolvedBy(admin);

        return mapComplaint(complaintRepository.save(complaint));
    }

    @Transactional
    public AdminComplaintDto dismissComplaint(String adminEmail, Long complaintId) {
        User admin = requireAdmin(adminEmail);
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        complaint.setStatus(ComplaintStatus.DISMISSED);
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setResolvedBy(admin);

        return mapComplaint(complaintRepository.save(complaint));
    }

    @Transactional
    public int deleteInactiveRequests(String adminEmail, int olderThanDays) {
        requireAdmin(adminEmail);

        int days = Math.max(1, olderThanDays);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return teammateRequestRepository.deleteByStatusNotAndCreatedAtBefore(RequestStatus.ACTIVE, cutoff);
    }

    private User requireAdmin(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        if (admin.getRole().getName() != RoleName.ADMIN || Boolean.TRUE.equals(admin.getIsBlocked())) {
            throw new RuntimeException("Admin privileges required");
        }

        return admin;
    }

    private void validateBlockTarget(User admin, User target) {
        if (admin.getId().equals(target.getId())) {
            throw new RuntimeException("Admin cannot block or unblock self");
        }

        if (target.getRole().getName() == RoleName.ADMIN) {
            throw new RuntimeException("Admin accounts cannot be blocked from this panel");
        }
    }

    private AdminUserDto mapUser(User user) {
        PlayerProfile profile = playerProfileRepository.findByUserId(user.getId()).orElse(null);

        return new AdminUserDto(
                user.getId(),
                user.getEmail(),
                user.getRole().getName(),
                user.getIsBlocked(),
                user.getCreatedAt(),
                profile == null ? null : profile.getId(),
                profile == null ? null : profile.getNickname(),
                profile == null ? null : profile.getKarma(),
                profile == null ? null : profile.getCompletedMatches()
        );
    }

    private AdminComplaintDto mapComplaint(Complaint complaint) {
        return new AdminComplaintDto(
                complaint.getId(),
                complaint.getReporter().getId(),
                nickname(complaint.getReporter().getId()),
                complaint.getReportedUser().getId(),
                nickname(complaint.getReportedUser().getId()),
                complaint.getReason(),
                complaint.getStatus(),
                complaint.getCreatedAt(),
                complaint.getResolvedAt(),
                complaint.getResolvedBy() == null ? null : complaint.getResolvedBy().getId()
        );
    }

    private String nickname(Long userId) {
        return playerProfileRepository.findByUserId(userId)
                .map(PlayerProfile::getNickname)
                .orElse("User " + userId);
    }
}
