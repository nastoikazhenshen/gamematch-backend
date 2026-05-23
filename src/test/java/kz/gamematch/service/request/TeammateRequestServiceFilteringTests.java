package kz.gamematch.service.request;

import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.TeammateRequest;
import kz.gamematch.entity.User;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TeammateRequestServiceFilteringTests {

    @Autowired
    private TeammateRequestService teammateRequestService;

    @Autowired
    private TeammateRequestRepository teammateRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameRepository gameRepository;

    private User author;
    private Game dota;
    private Game cs2;
    private LocalDateTime baseTime;

    @BeforeEach
    void setUp() {
        teammateRequestRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        author = createAuthor();
        dota = gameRepository.findByName("Dota 2").orElseThrow();
        cs2 = gameRepository.findByName("CS2").orElseThrow();
        baseTime = LocalDateTime.of(2026, 5, 24, 20, 0);
    }

    @Test
    void filtersActiveRequestsByGameRoleRanksAndDesiredTimeRange() {
        TeammateRequest expected = createRequest(
                dota,
                "Need support",
                "Support",
                "Archon",
                "Legend",
                baseTime.plusHours(1),
                RequestStatus.ACTIVE
        );
        createRequest(dota, "Wrong role", "Carry", "Archon", "Legend", baseTime.plusHours(1), RequestStatus.ACTIVE);
        createRequest(dota, "Wrong min rank", "Support", "Crusader", "Legend", baseTime.plusHours(1), RequestStatus.ACTIVE);
        createRequest(dota, "Wrong max rank", "Support", "Archon", "Ancient", baseTime.plusHours(1), RequestStatus.ACTIVE);
        createRequest(cs2, "Wrong game", "Support", "Archon", "Legend", baseTime.plusHours(1), RequestStatus.ACTIVE);
        createRequest(dota, "Too early", "Support", "Archon", "Legend", baseTime.minusHours(1), RequestStatus.ACTIVE);
        createRequest(dota, "Closed", "Support", "Archon", "Legend", baseTime.plusHours(1), RequestStatus.CLOSED);

        Page<TeammateRequestResponseDto> result = teammateRequestService.searchActiveRequests(
                dota.getId(),
                "support",
                "archon",
                "legend",
                baseTime,
                baseTime.plusHours(2),
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(TeammateRequestResponseDto::getId)
                .containsExactly(expected.getId());
    }

    @Test
    void returnsRequestedPageAndSortOrder() {
        createRequest(dota, "First", "Support", "Archon", "Legend", baseTime, RequestStatus.ACTIVE);
        TeammateRequest second = createRequest(dota, "Second", "Support", "Archon", "Legend", baseTime.plusHours(1), RequestStatus.ACTIVE);
        createRequest(dota, "Third", "Support", "Archon", "Legend", baseTime.plusHours(2), RequestStatus.ACTIVE);

        Page<TeammateRequestResponseDto> result = teammateRequestService.searchActiveRequests(
                dota.getId(),
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "createdAt"))
        );

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(TeammateRequestResponseDto::getId)
                .containsExactly(second.getId());
    }

    private User createAuthor() {
        Role role = roleRepository.findByName(RoleName.PLAYER).orElseThrow();

        User user = new User();
        user.setEmail("filter-" + UUID.randomUUID() + "@example.com");
        user.setPassword("password");
        user.setRole(role);
        user.setIsBlocked(false);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(savedUser);
        profile.setNickname("filter" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        profile.setKarma(BigDecimal.ZERO);
        profile.setCompletedMatches(0);
        playerProfileRepository.save(profile);

        return savedUser;
    }

    private TeammateRequest createRequest(
            Game game,
            String title,
            String requiredRole,
            String minRank,
            String maxRank,
            LocalDateTime desiredPlayTime,
            RequestStatus status
    ) {
        TeammateRequest request = new TeammateRequest();
        request.setAuthor(author);
        request.setGame(game);
        request.setTitle(title);
        request.setRequiredRole(requiredRole);
        request.setMinRank(minRank);
        request.setMaxRank(maxRank);
        request.setDesiredPlayTime(desiredPlayTime);
        request.setStatus(status);
        request.setCreatedAt(desiredPlayTime);

        return teammateRequestRepository.save(request);
    }
}
