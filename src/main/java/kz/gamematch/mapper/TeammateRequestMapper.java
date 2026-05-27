package kz.gamematch.mapper;

import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.entity.TeammateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeammateRequestMapper {

    private final PlayerNicknameResolver nicknameResolver;

    public TeammateRequestResponseDto toDto(TeammateRequest request) {
        Long authorId = request.getAuthor().getId();
        return new TeammateRequestResponseDto(
                request.getId(),
                authorId,
                nicknameResolver.requiredNickname(authorId, "Author profile not found"),
                request.getGame().getId(),
                request.getGame().getName(),
                request.getTitle(),
                request.getDescription(),
                request.getRequiredRole(),
                request.getMinRank(),
                request.getMaxRank(),
                request.getDesiredPlayTime(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
