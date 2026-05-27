package kz.gamematch.dto.admin;

public record AdminDashboardDto(
        long totalUsers,
        long blockedUsers,
        long openComplaints,
        long activeRequests,
        long inactiveRequestsOlderThanSevenDays
) {
}
