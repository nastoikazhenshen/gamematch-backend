package kz.gamematch.service.profile;

import kz.gamematch.dto.profile.PlayerGameResponseDto;
import kz.gamematch.dto.profile.UpsertPlayerGameRequestDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.Role;
import kz.gamematch.entity.RoleName;
import kz.gamematch.entity.User;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerGameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RoleRepository;
import kz.gamematch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProfilePlayerGameServiceTests {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PlayerGameRepository playerGameRepository;

    @Autowired
    private PlayerProfileRepository playerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GameRepository gameRepository;

    private User player;
    private User otherPlayer;
    private Game dota;
    private Game valorant;

    @BeforeEach
    void setUp() {
        playerGameRepository.deleteAll();
        playerProfileRepository.deleteAll();
        userRepository.deleteAll();

        player = createUserWithProfile("games");
        otherPlayer = createUserWithProfile("other");
        dota = gameRepository.findByName("Dota 2").orElseThrow();
        valorant = gameRepository.findByName("Valorant").orElseThrow();
    }

    @Test
    void addsSeveralGamesToProfileAndReturnsRankImages() {
        PlayerGameResponseDto dotaGame = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Archon", "Support")
        );
        PlayerGameResponseDto valorantGame = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(valorant.getId(), "Gold", "Duelist")
        );

        List<PlayerGameResponseDto> games = profileService.getProfileGamesByUserId(player.getId());

        assertThat(games)
                .extracting(PlayerGameResponseDto::getGameName)
                .containsExactlyInAnyOrder("Dota 2", "Valorant");
        assertThat(dotaGame.getRankImageUrl()).isNotBlank();
        assertThat(valorantGame.getRankImageUrl()).isNotBlank();
    }

    @Test
    void updatesExistingProfileGameInsteadOfCreatingDuplicate() {
        PlayerGameResponseDto created = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Archon", "Support")
        );

        PlayerGameResponseDto updated = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Legend", "Carry")
        );

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getRank()).isEqualTo("Legend");
        assertThat(updated.getMainRole()).isEqualTo("Carry");
        assertThat(profileService.getProfileGamesByUserId(player.getId())).hasSize(1);
    }

    @Test
    void rejectsRankFromAnotherGame() {
        assertThatThrownBy(() -> profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Radiant", "Support")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Rank does not belong to selected game");
    }

    @Test
    void onlyProfileOwnerCanUpdateOrDeletePlayerGame() {
        PlayerGameResponseDto created = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Archon", "Support")
        );

        assertThatThrownBy(() -> profileService.updatePlayerGame(
                otherPlayer.getId(),
                created.getId(),
                gameRequest(dota.getId(), "Legend", "Carry")
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only profile owner can update player game");

        assertThatThrownBy(() -> profileService.deletePlayerGame(otherPlayer.getId(), created.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Only profile owner can delete player game");
    }

    @Test
    void deletesProfileGame() {
        PlayerGameResponseDto created = profileService.addOrUpdatePlayerGame(
                player.getId(),
                gameRequest(dota.getId(), "Archon", "Support")
        );

        profileService.deletePlayerGame(player.getId(), created.getId());

        assertThat(profileService.getProfileGamesByUserId(player.getId())).isEmpty();
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

    private UpsertPlayerGameRequestDto gameRequest(Long gameId, String rank, String mainRole) {
        UpsertPlayerGameRequestDto request = new UpsertPlayerGameRequestDto();
        request.setGameId(gameId);
        request.setRank(rank);
        request.setMainRole(mainRole);
        return request;
    }
}
