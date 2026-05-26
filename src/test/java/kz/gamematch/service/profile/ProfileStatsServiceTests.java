package kz.gamematch.service.profile;

import kz.gamematch.dto.profile.PlayerStatsResponseDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.RequestResponse;
import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.TeamMember;
import kz.gamematch.entity.TeammateRequest;
import kz.gamematch.entity.User;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RequestResponseRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProfileStatsServiceTests {

    @Autowired
    private ProfileService profileService;

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
    private User responder;
    private Game game;

    @BeforeEach
    void setUp() {
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        requestResponseRepository.deleteAll();
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        game = gameRepository.findByName("Dota 2").orElseThrow();
        author = createUserWithProfile("author");
        responder = createUserWithProfile("responder");
    }

    @Test
    void calculatesPlayerStatsFromTeamsAndResponses() {
        TeammateRequest firstRequest = createRequest(author);
        TeammateRequest secondRequest = createRequest(author);
        TeammateRequest thirdRequest = createRequest(author);

        RequestResponse accepted = createResponse(firstRequest, responder, ResponseStatus.ACCEPTED);
        createResponse(secondRequest, responder, ResponseStatus.REJECTED);
        createResponse(thirdRequest, responder, ResponseStatus.PENDING);
        createTeam(firstRequest, accepted, author, responder);

        PlayerStatsResponseDto stats = profileService.getStatsByUserId(responder.getId());

        assertThat(stats.getPlayedMatches()).isEqualTo(1);
        assertThat(stats.getSentResponses()).isEqualTo(3);
        assertThat(stats.getAcceptedResponses()).isEqualTo(1);
        assertThat(stats.getAcceptedResponseRate()).isEqualTo(33);
        assertThat(stats.getAuthoredRequests()).isZero();
    }

    @Test
    void returnsZeroAcceptanceRateWhenPlayerHasNoResponses() {
        PlayerStatsResponseDto stats = profileService.getStatsByUserId(author.getId());

        assertThat(stats.getSentResponses()).isZero();
        assertThat(stats.getAcceptedResponses()).isZero();
        assertThat(stats.getAcceptedResponseRate()).isZero();
    }

    @Test
    void resolvesStatsByProfileId() {
        createRequest(author);

        PlayerProfile profile = playerProfileRepository.findByUserId(author.getId()).orElseThrow();
        PlayerStatsResponseDto stats = profileService.getStatsByProfileId(profile.getId());

        assertThat(stats.getUserId()).isEqualTo(author.getId());
        assertThat(stats.getAuthoredRequests()).isEqualTo(1);
    }

    @Test
    void suggestsPlayersByAuthoredRequestCount() {
        createRequest(author);
        createRequest(author);
        createRequest(responder);

        var suggestedPlayers = profileService.getSuggestedPlayers(author.getId(), 10);

        assertThat(suggestedPlayers)
                .extracting(player -> player.getUserId())
                .doesNotContain(author.getId())
                .contains(responder.getId());
        assertThat(suggestedPlayers.get(0).getRequestCount()).isEqualTo(1);
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

    private TeammateRequest createRequest(User requestAuthor) {
        TeammateRequest request = new TeammateRequest();
        request.setAuthor(requestAuthor);
        request.setGame(game);
        request.setTitle("Need teammate");
        request.setRequiredRole("Support");
        request.setMinRank("Archon");
        request.setMaxRank("Legend");
        request.setDesiredPlayTime(LocalDateTime.now().plusHours(1));
        request.setStatus(RequestStatus.ACTIVE);
        request.setCreatedAt(LocalDateTime.now());
        return teammateRequestRepository.save(request);
    }

    private RequestResponse createResponse(TeammateRequest request, User responseAuthor, ResponseStatus status) {
        RequestResponse response = new RequestResponse();
        response.setRequest(request);
        response.setResponder(responseAuthor);
        response.setMessage("Ready");
        response.setStatus(status);
        response.setCreatedAt(LocalDateTime.now());
        return requestResponseRepository.save(response);
    }

    private void createTeam(TeammateRequest request, RequestResponse acceptedResponse, User first, User second) {
        Team team = new Team();
        team.setRequest(request);
        team.setAcceptedResponse(acceptedResponse);
        team.setGame(game);
        team.setCreatedAt(LocalDateTime.now());
        Team savedTeam = teamRepository.save(team);

        teamMemberRepository.save(createMember(savedTeam, first));
        teamMemberRepository.save(createMember(savedTeam, second));
    }

    private TeamMember createMember(Team team, User user) {
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setCreatedAt(LocalDateTime.now());
        return member;
    }
}
