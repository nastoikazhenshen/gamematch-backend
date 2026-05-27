package kz.gamematch.dto.admin;

import kz.gamematch.entity.RoleName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminUserDto(
        Long id,
        String email,
        RoleName role,
        Boolean blocked,
        LocalDateTime createdAt,
        Long profileId,
        String nickname,
        BigDecimal karma,
        Integer completedMatches
) {
}
