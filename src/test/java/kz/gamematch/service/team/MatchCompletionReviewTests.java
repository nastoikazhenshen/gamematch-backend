package kz.gamematch.service.team;

import kz.gamematch.dto.team.CreatePlayerReviewRequestDto;
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
import kz.gamematch.repository.PlayerReviewRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class MatchCompletionReviewTests {

    @Autowired
    private TeamService teamService;

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

    private User author;
    private User responder;
    private User outsider;
    private Team team;

    @BeforeEach
    void setUp() {
        playerReviewRepository.deleteAll();
        teamMemberRepository.deleteAll();
        teamRepository.deleteAll();
        requestResponseRepository.deleteAll();
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        Game game = gameRepository.findByName("Dota 2").orElseThrow();
        author = createUserWithProfile("author");
        responder = createUserWithProfile("responder");
        outsider = createUserWithProfile("outsider");
        team = createTeam(game, author, responder);
    }

    @Test
    void teamMemberCanCompleteMatchAndCompletedMatchesAreUpdated() {
        var completedTeam = teamService.completeMatch(team.getId(), author.getId());

        assertThat(completedTeam.getCompletedAt()).isNotNull();
        assertThat(completedTeam.getCompletedByUserId()).isEqualTo(author.getId());
        assertThat(playerProfileRepository.findByUserId(author.getId()).orElseThrow().getCompletedMatches()).isEqualTo(1);
        assertThat(playerProfileRepository.findByUserId(responder.getId()).orElseThrow().getCompletedMatches()).isEqualTo(1);
    }

    @Test
    void reviewAfterCompletedMatchRecalculatesKarmaFromAverageStars() {
        teamService.completeMatch(team.getId(), author.getId());

        teamService.reviewPlayer(team.getId(), review(author.getId(), responder.getId(), 5, "Great support"));
        teamService.reviewPlayer(team.getId(), review(responder.getId(), author.getId(), 3, "Okay game"));
        Team secondTeam = createTeam(gameRepository.findByName("Dota 2").orElseThrow(), outsider, responder);
        teamService.completeMatch(secondTeam.getId(), outsider.getId());
        teamService.reviewPlayer(secondTeam.getId(), review(outsider.getId(), responder.getId(), 3, "Good enough"));

        PlayerProfile responderProfile = playerProfileRepository.findByUserId(responder.getId()).orElseThrow();
        PlayerProfile authorProfile = playerProfileRepository.findByUserId(author.getId()).orElseThrow();

        assertThat(responderProfile.getKarma()).isEqualByComparingTo(new BigDecimal("4.00"));
        assertThat(authorProfile.getKarma()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(playerReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(responder.getId())).hasSize(2);
    }

    @Test
    void cannotReviewBeforeCompletionOrReviewYourself() {
        assertThatThrownBy(() -> teamService.reviewPlayer(team.getId(), review(author.getId(), responder.getId(), 5, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Match must be completed before review");

        teamService.completeMatch(team.getId(), author.getId());

        assertThatThrownBy(() -> teamService.reviewPlayer(team.getId(), review(author.getId(), author.getId(), 5, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot review yourself");
    }

    @Test
    void outsiderCannotCompleteOrReviewMatch() {
        assertThatThrownBy(() -> teamService.completeMatch(team.getId(), outsider.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only team members can complete match");

        teamService.completeMatch(team.getId(), author.getId());

        assertThatThrownBy(() -> teamService.reviewPlayer(team.getId(), review(outsider.getId(), responder.getId(), 5, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only team members can review each other");
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

    private Team createTeam(Game game, User first, User second) {
        TeammateRequest request = new TeammateRequest();
        request.setAuthor(first);
        request.setGame(game);
        request.setTitle("Need teammate");
        request.setDesiredPlayTime(LocalDateTime.now().plusHours(1));
        request.setStatus(RequestStatus.CLOSED);
        request.setCreatedAt(LocalDateTime.now());
        TeammateRequest savedRequest = teammateRequestRepository.save(request);

        RequestResponse response = new RequestResponse();
        response.setRequest(savedRequest);
        response.setResponder(second);
        response.setStatus(ResponseStatus.ACCEPTED);
        response.setCreatedAt(LocalDateTime.now());
        RequestResponse savedResponse = requestResponseRepository.save(response);

        Team createdTeam = new Team();
        createdTeam.setRequest(savedRequest);
        createdTeam.setAcceptedResponse(savedResponse);
        createdTeam.setGame(game);
        createdTeam.setCreatedAt(LocalDateTime.now());
        Team savedTeam = teamRepository.save(createdTeam);

        teamMemberRepository.save(member(savedTeam, first));
        teamMemberRepository.save(member(savedTeam, second));
        return savedTeam;
    }

    private TeamMember member(Team team, User user) {
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setCreatedAt(LocalDateTime.now());
        return member;
    }

    private CreatePlayerReviewRequestDto review(Long reviewerId, Long reviewedUserId, int stars, String comment) {
        CreatePlayerReviewRequestDto request = new CreatePlayerReviewRequestDto();
        request.setReviewerId(reviewerId);
        request.setReviewedUserId(reviewedUserId);
        request.setStars(stars);
        request.setComment(comment);
        return request;
    }
}
