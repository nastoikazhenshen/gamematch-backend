package kz.gamematch.service.cabinet;

import kz.gamematch.entity.*;
import kz.gamematch.repository.*;
import kz.gamematch.service.profile.KarmaLeaderboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlayerCabinetLeaderboardTests {

    @Autowired
    private PlayerCabinetService playerCabinetService;

    @Autowired
    private KarmaLeaderboardService karmaLeaderboardService;

    @Autowired
    private PlayerReviewRepository playerReviewRepository;

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

    private Game game;
    private User author;
    private User responder;
    private User outsider;

    @BeforeEach
    void setUp() {
        playerReviewRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        requestResponseRepository.deleteAll();
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        game = gameRepository.findByName("Dota 2").orElseThrow();
        author = createUser("author", new BigDecimal("4.80"), 7);
        responder = createUser("responder", new BigDecimal("4.90"), 4);
        outsider = createUser("outsider", new BigDecimal("4.90"), 9);
    }

    @Test
    void cabinetReturnsOnlyCurrentUsersResponsesGroupedByStatus() {
        TeammateRequest request = createRequest(author, "Need support");
        TeammateRequest otherRequest = createRequest(author, "Need mid");
        createResponse(request, responder, ResponseStatus.PENDING, "I can play");
        createResponse(otherRequest, responder, ResponseStatus.ACCEPTED, "Invite me");
        createResponse(request, outsider, ResponseStatus.PENDING, "Outsider response");

        var pending = playerCabinetService.getMyResponses(responder.getEmail(), ResponseStatus.PENDING);
        var all = playerCabinetService.getMyResponses(responder.getEmail(), null);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).requestTitle()).isEqualTo("Need support");
        assertThat(pending.get(0).authorNickname()).startsWith("author");
        assertThat(all).hasSize(2);
        assertThat(all).extracting("status")
                .containsExactlyInAnyOrder(ResponseStatus.PENDING, ResponseStatus.ACCEPTED);
    }

    @Test
    void cabinetReturnsCompletedMatchHistoryAndBothReviewLists() {
        TeammateRequest request = createRequest(author, "Need teammate");
        RequestResponse response = createResponse(request, responder, ResponseStatus.ACCEPTED, "Ready");
        Team team = createCompletedTeam(request, response, author);

        createReview(team, author, responder, 5, "Great teammate");
        createReview(team, responder, author, 4, "Good captain");

        var history = playerCabinetService.getMatchHistory(responder.getEmail());
        var receivedReviews = playerCabinetService.getReceivedReviews(responder.getEmail());
        var givenReviews = playerCabinetService.getGivenReviews(responder.getEmail());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).teamId()).isEqualTo(team.getId());
        assertThat(history.get(0).teammates()).extracting("nickname").allMatch(name -> ((String) name).startsWith("author"));
        assertThat(receivedReviews).hasSize(1);
        assertThat(receivedReviews.get(0).getReviewerNickname()).startsWith("author");
        assertThat(receivedReviews.get(0).getStars()).isEqualTo(5);
        assertThat(givenReviews).hasSize(1);
        assertThat(givenReviews.get(0).getReviewedUserNickname()).startsWith("author");
        assertThat(givenReviews.get(0).getStars()).isEqualTo(4);
    }

    @Test
    void karmaLeaderboardOrdersByKarmaThenCompletedMatchesThenNickname() {
        var leaders = karmaLeaderboardService.getLeaders(3);

        assertThat(leaders).hasSize(3);
        assertThat(leaders.get(0).nickname()).startsWith("outsider");
        assertThat(leaders.get(1).nickname()).startsWith("responder");
        assertThat(leaders.get(2).nickname()).startsWith("author");
        assertThat(leaders).extracting("position").containsExactly(1, 2, 3);
    }

    private User createUser(String prefix, BigDecimal karma, int completedMatches) {
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
        profile.setKarma(karma);
        profile.setCompletedMatches(completedMatches);
        playerProfileRepository.save(profile);

        return savedUser;
    }

    private TeammateRequest createRequest(User author, String title) {
        TeammateRequest request = new TeammateRequest();
        request.setAuthor(author);
        request.setGame(game);
        request.setTitle(title);
        request.setRequiredRole("Support");
        request.setStatus(RequestStatus.ACTIVE);
        request.setCreatedAt(LocalDateTime.now());
        request.setDesiredPlayTime(LocalDateTime.now().plusHours(2));
        return teammateRequestRepository.save(request);
    }

    private RequestResponse createResponse(
            TeammateRequest request,
            User responder,
            ResponseStatus status,
            String message
    ) {
        RequestResponse response = new RequestResponse();
        response.setRequest(request);
        response.setResponder(responder);
        response.setStatus(status);
        response.setMessage(message);
        response.setCreatedAt(LocalDateTime.now());
        return requestResponseRepository.save(response);
    }

    private Team createCompletedTeam(TeammateRequest request, RequestResponse response, User completedBy) {
        Team team = new Team();
        team.setRequest(request);
        team.setAcceptedResponse(response);
        team.setGame(game);
        team.setCreatedAt(LocalDateTime.now().minusHours(3));
        team.setCompletedAt(LocalDateTime.now().minusHours(1));
        team.setCompletedBy(completedBy);
        Team savedTeam = teamRepository.save(team);

        createTeamMember(savedTeam, author);
        createTeamMember(savedTeam, responder);
        return savedTeam;
    }

    private void createTeamMember(Team team, User user) {
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setCreatedAt(LocalDateTime.now());
        teamMemberRepository.save(member);
    }

    private void createReview(Team team, User reviewer, User reviewed, int stars, String comment) {
        PlayerReview review = new PlayerReview();
        review.setTeam(team);
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewed);
        review.setStars(stars);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        playerReviewRepository.save(review);
    }
}
