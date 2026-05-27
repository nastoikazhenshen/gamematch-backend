package kz.gamematch.dto.cabinet;

import kz.gamematch.entity.ResponseStatus;

import java.time.LocalDateTime;

public record MyResponseDto(
        Long responseId,
        Long requestId,
        String requestTitle,
        String gameName,
        Long authorId,
        String authorNickname,
        String message,
        ResponseStatus status,
        LocalDateTime responseCreatedAt,
        LocalDateTime desiredPlayTime
) {
}
