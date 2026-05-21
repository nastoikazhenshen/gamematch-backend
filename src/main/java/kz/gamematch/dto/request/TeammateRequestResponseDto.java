package kz.gamematch.dto.request;

import kz.gamematch.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TeammateRequestResponseDto {

    private Long id;

    private Long authorId;

    private String authorNickname;

    private Long gameId;

    private String gameName;

    private String title;

    private String description;

    private String requiredRole;

    private String minRank;

    private String maxRank;

    private LocalDateTime desiredPlayTime;

    private RequestStatus status;

    private LocalDateTime createdAt;
}