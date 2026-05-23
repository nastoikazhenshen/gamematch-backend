package kz.gamematch.controller.chat;

import jakarta.validation.Valid;
import kz.gamematch.dto.chat.ChatMessageDto;
import kz.gamematch.dto.chat.SendChatMessageDto;
import kz.gamematch.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatMessageDto sendMessage(
            @PathVariable Long teamId,
            @Valid @RequestBody SendChatMessageDto dto
    ) {
        return chatService.sendMessage(teamId, dto);
    }

    @GetMapping
    public List<ChatMessageDto> getMessages(
            @PathVariable Long teamId,
            @RequestParam Long userId
    ) {
        return chatService.getMessages(teamId, userId);
    }
}
