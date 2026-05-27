package kz.gamematch.dto.admin;

import kz.gamematch.entity.ComplaintStatus;

import java.time.LocalDateTime;

public record AdminComplaintDto(
        Long id,
        Long reporterId,
        String reporterNickname,
        Long reportedUserId,
        String reportedNickname,
        String reason,
        ComplaintStatus status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        Long resolvedByUserId
) {
}
