package kz.gamematch.controller.chat;

import jakarta.validation.Valid;
import kz.gamematch.dto.chat.ChatMessageDto;
import kz.gamematch.dto.chat.SendChatMessageDto;
import kz.gamematch.security.CurrentUserService;
import kz.gamematch.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ChatMessageDto sendMessage(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails currentUser,
            @Valid @RequestBody SendChatMessageDto dto
    ) {
        dto.setSenderId(currentUserService.userId(currentUser));
        return chatService.sendMessage(teamId, dto);
    }

    @GetMapping
    public List<ChatMessageDto> getMessages(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        return chatService.getMessages(teamId, currentUserService.userId(currentUser));
    }
}
