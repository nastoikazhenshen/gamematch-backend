package kz.gamematch.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageDto {

    private Long id;

    private Long teamId;

    private Long senderId;

    private String senderNickname;

    private String content;

    private LocalDateTime createdAt;
}
