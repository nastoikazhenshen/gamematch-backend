package kz.gamematch.service.chat;

import kz.gamematch.dto.chat.ChatMessageDto;
import kz.gamematch.dto.chat.SendChatMessageDto;
import kz.gamematch.entity.ChatMessage;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.User;
import kz.gamematch.mapper.ChatMessageMapper;
import kz.gamematch.repository.ChatMessageRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper chatMessageMapper;

    @Transactional
    public ChatMessageDto sendMessage(Long teamId, SendChatMessageDto dto) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, sender.getId())) {
            throw new RuntimeException("Only team members can send chat messages");
        }

        ChatMessage message = new ChatMessage();
        message.setTeam(team);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setCreatedAt(LocalDateTime.now());

        return chatMessageMapper.toDto(chatMessageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getMessages(Long teamId, Long userId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException("Team not found");
        }

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("Only team members can read chat messages");
        }

        return chatMessageRepository.findByTeamIdOrderByCreatedAtAsc(teamId)
                .stream()
                .map(chatMessageMapper::toDto)
                .toList();
    }
}
