package kz.gamematch.mapper;

import kz.gamematch.dto.response.RequestResponseDto;
import kz.gamematch.entity.RequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestResponseMapper {

    private final PlayerNicknameResolver nicknameResolver;

    public RequestResponseDto toDto(RequestResponse response) {
        Long responderId = response.getResponder().getId();
        return new RequestResponseDto(
                response.getId(),
                response.getRequest().getId(),
                responderId,
                nicknameResolver.requiredNickname(responderId, "Responder profile not found"),
                response.getMessage(),
                response.getStatus(),
                response.getCreatedAt()
        );
    }
}
