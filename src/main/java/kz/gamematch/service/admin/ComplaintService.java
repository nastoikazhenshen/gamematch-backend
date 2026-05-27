package kz.gamematch.service.admin;

import kz.gamematch.dto.admin.AdminComplaintDto;
import kz.gamematch.dto.admin.CreateComplaintDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.ComplaintRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;

    @Transactional
    public AdminComplaintDto createComplaint(String reporterEmail, CreateComplaintDto request) {
        User reporter = userRepository.findById(request.getReporterId())
                .orElseThrow(() -> new RuntimeException("Reporter not found"));
        User reportedUser = userRepository.findById(request.getReportedUserId())
                .orElseThrow(() -> new RuntimeException("Reported user not found"));

        if (!reporter.getEmail().equals(reporterEmail)) {
            throw new RuntimeException("Reporter must match authenticated user");
        }

        if (reporter.getId().equals(reportedUser.getId())) {
            throw new RuntimeException("Cannot report yourself");
        }

        Complaint complaint = new Complaint();
        complaint.setReporter(reporter);
        complaint.setReportedUser(reportedUser);
        complaint.setReason(request.getReason().trim());
        complaint.setStatus(ComplaintStatus.OPEN);
        complaint.setCreatedAt(LocalDateTime.now());

        return mapComplaint(complaintRepository.save(complaint));
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
