package kz.gamematch.mapper;

import kz.gamematch.dto.chat.ChatMessageDto;
import kz.gamematch.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageMapper {

    private final PlayerNicknameResolver nicknameResolver;

    public ChatMessageDto toDto(ChatMessage message) {
        Long senderId = message.getSender().getId();
        return new ChatMessageDto(
                message.getId(),
                message.getTeam().getId(),
                senderId,
                nicknameResolver.requiredNickname(senderId, "Sender profile not found"),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
