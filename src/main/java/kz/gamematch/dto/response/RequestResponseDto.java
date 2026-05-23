package kz.gamematch.dto.response;

import kz.gamematch.entity.ResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RequestResponseDto {

    private Long id;

    private Long requestId;

    private Long responderId;

    private String responderNickname;

    private String message;

    private ResponseStatus status;

    private LocalDateTime createdAt;
}