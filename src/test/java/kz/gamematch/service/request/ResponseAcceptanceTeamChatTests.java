package kz.gamematch.service.request;

import kz.gamematch.dto.chat.SendChatMessageDto;
import kz.gamematch.entity.ChatMessage;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.RequestResponse;
import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.TeammateRequest;
import kz.gamematch.entity.User;
import kz.gamematch.repository.ChatMessageRepository;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RequestResponseRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import kz.gamematch.service.chat.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ResponseAcceptanceTeamChatTests {

    @Autowired
    private RequestResponseService requestResponseService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RequestResponseRepository requestResponseRepository;

    @Autowired
    private TeammateRequestRepository teammateRequestRepository;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameRepository gameRepository;

    private User author;
    private User acceptedResponder;
    private User rejectedResponder;
    private User outsider;
    private Game game;
    private TeammateRequest request;
    private RequestResponse acceptedResponse;
    private RequestResponse otherResponse;

    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        requestResponseRepository.deleteAll();
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        game = gameRepository.findByName("Dota 2").orElseThrow();
        author = createUserWithProfile("author");
        acceptedResponder = createUserWithProfile("accepted");
        rejectedResponder = createUserWithProfile("rejected");
        outsider = createUserWithProfile("outsider");
        request = createRequest(author);
        acceptedResponse = createResponse(request, acceptedResponder, "I can support");
        otherResponse = createResponse(request, rejectedResponder, "I can mid");
    }

    @Test
    void acceptingResponseCreatesTeamWithChatAndRejectsOtherPendingResponses() {
        requestResponseService.acceptResponse(acceptedResponse.getId(), author.getId());

        TeammateRequest updatedRequest = teammateRequestRepository.findById(request.getId()).orElseThrow();
        RequestResponse updatedAcceptedResponse = requestResponseRepository.findById(acceptedResponse.getId())
                .orElseThrow();
        RequestResponse updatedOtherResponse = requestResponseRepository.findById(otherResponse.getId())
                .orElseThrow();
        Team team = teamRepository.findByRequestId(request.getId()).orElseThrow();

        assertThat(updatedRequest.getStatus()).isEqualTo(RequestStatus.CLOSED);
        assertThat(updatedAcceptedResponse.getStatus()).isEqualTo(ResponseStatus.ACCEPTED);
        assertThat(updatedOtherResponse.getStatus()).isEqualTo(ResponseStatus.REJECTED);
        assertThat(team.getAcceptedResponse().getId()).isEqualTo(acceptedResponse.getId());
        assertThat(teamMemberRepository.findByTeamId(team.getId()))
                .extracting(member -> member.getUser().getId())
                .containsExactlyInAnyOrder(author.getId(), acceptedResponder.getId());

        SendChatMessageDto messageDto = new SendChatMessageDto();
        messageDto.setSenderId(acceptedResponder.getId());
        messageDto.setContent("Ready to play?");

        var sentMessage = chatService.sendMessage(team.getId(), messageDto);
        var messages = chatService.getMessages(team.getId(), author.getId());

        assertThat(sentMessage.getContent()).isEqualTo("Ready to play?");
        assertThat(messages)
                .extracting(message -> message.getContent())
                .containsExactly("Ready to play?");
    }

    @Test
    void nonTeamMemberCannotUseTeamChat() {
        requestResponseService.acceptResponse(acceptedResponse.getId(), author.getId());
        Team team = teamRepository.findByRequestId(request.getId()).orElseThrow();

        SendChatMessageDto messageDto = new SendChatMessageDto();
        messageDto.setSenderId(outsider.getId());
        messageDto.setContent("Let me in");

        assertThatThrownBy(() -> chatService.sendMessage(team.getId(), messageDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only team members can send chat messages");

        assertThatThrownBy(() -> chatService.getMessages(team.getId(), outsider.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only team members can read chat messages");
    }

    private User createUserWithProfile(String prefix) {
        Role role = roleRepository.findByName(RoleName.PLAYER).orElseThrow();

        User user = new User();
        user.setEmail(prefix + "-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setIsBlocked(false);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(savedUser);
        profile.setNickname(prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        profile.setKarma(BigDecimal.ZERO);
        profile.setCompletedMatches(0);
        playerProfileRepository.save(profile);

        return savedUser;
    }

    private TeammateRequest createRequest(User author) {
        TeammateRequest teammateRequest = new TeammateRequest();
        teammateRequest.setAuthor(author);
        teammateRequest.setGame(game);
        teammateRequest.setTitle("Need teammate");
        teammateRequest.setRequiredRole("Support");
        teammateRequest.setMinRank("Archon");
        teammateRequest.setMaxRank("Legend");
        teammateRequest.setDesiredPlayTime(LocalDateTime.now().plusHours(1));
        teammateRequest.setStatus(RequestStatus.ACTIVE);
        teammateRequest.setCreatedAt(LocalDateTime.now());

        return teammateRequestRepository.save(teammateRequest);
    }

    private RequestResponse createResponse(TeammateRequest request, User responder, String message) {
        RequestResponse response = new RequestResponse();
        response.setRequest(request);
        response.setResponder(responder);
        response.setMessage(message);
        response.setStatus(ResponseStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());

        return requestResponseRepository.save(response);
    }
}
